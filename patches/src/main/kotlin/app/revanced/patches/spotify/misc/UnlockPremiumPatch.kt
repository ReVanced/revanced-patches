package app.revanced.patches.spotify.misc

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableClass
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.spotify.misc.extension.sharedExtensionPatch
import app.revanced.patches.spotify.shared.IS_SPOTIFY_LEGACY_APP_TARGET
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import app.revanced.util.returnEarly
import app.revanced.util.toPublicAccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference
import java.util.logging.Logger

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/spotify/misc/UnlockPremiumPatch;"

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Spotify Premium features. Server-sided features like downloading songs are still locked.",
) {
    compatibleWith("com.spotify.music")

    dependsOn(
        sharedExtensionPatch,
        // Currently there is no easy way to make a mandatory patch,
        // so for now this is a dependent of this patch.
        //
        // FIXME: Modifying string resources (such as adding patch strings)
        // is currently failing with ReVanced Manager.
        // checkEnvironmentPatch,
    )

    execute {
        fun MutableClass.publicizeField(fieldName: String) {
            fields.first { it.name == fieldName }.apply {
                // Add public and remove private flag.
                accessFlags = accessFlags.toPublicAccessFlags()
            }
        }

        // Make _value accessible so that it can be overridden in the extension.
        val accountFingerprint by accountAttributeFingerprint
        accountFingerprint.classDef.publicizeField("value_")

        // Override the attributes map in the getter method.
        val productFingerprint by productStateProtoGetMapFingerprint
        productFingerprint.method.apply {
            val getAttributesMapIndex = indexOfFirstInstructionOrThrow(Opcode.IGET_OBJECT)
            val attributesMapRegister = getInstruction<TwoRegisterInstruction>(getAttributesMapIndex).registerA

            addInstruction(
                getAttributesMapIndex + 1,
                "invoke-static { v$attributesMapRegister }, " +
                        "$EXTENSION_CLASS_DESCRIPTOR->overrideAttributes(Ljava/util/Map;)V"
            )
        }


        // Add the query parameter trackRows to show popular tracks in the artist page.
        buildQueryParametersFingerprint.method.apply {
            val addQueryParameterConditionIndex = indexOfFirstInstructionReversedOrThrow(
                buildQueryParametersFingerprint.stringMatches.first().index, Opcode.IF_EQZ
            )

            removeInstruction(addQueryParameterConditionIndex)
        }


        if (IS_SPOTIFY_LEGACY_APP_TARGET) {
            Logger.getLogger(this::class.java.name).warning(
                "Patching a legacy Spotify version.  Patch functionality may be limited."
            )
            return@execute
        }


        // Enable choosing a specific song/artist via Google Assistant.
        contextFromJsonFingerprint.method.apply {
            val insertIndex = contextFromJsonFingerprint.patternMatch.startIndex
            // Both the URI and URL need to be modified.
            val registerUrl = getInstruction<FiveRegisterInstruction>(insertIndex).registerC
            val registerUri = getInstruction<FiveRegisterInstruction>(insertIndex + 2).registerD

            val extensionMethodDescriptor = "$EXTENSION_CLASS_DESCRIPTOR->" +
                    "removeStationString(Ljava/lang/String;)Ljava/lang/String;"

            addInstructions(
                insertIndex,
                """
                    invoke-static { v$registerUrl }, $extensionMethodDescriptor
                    move-result-object v$registerUrl
                    invoke-static { v$registerUri }, $extensionMethodDescriptor
                    move-result-object v$registerUri
                """
            )
        }


        // Disable forced shuffle when asking for an album/playlist via Google Assistant.
        readPlayerOptionOverridesFingerprint.method.apply {
            val shufflingContextCallIndex = indexOfFirstInstructionOrThrow {
                getReference<MethodReference>()?.name == "shufflingContext"
            }
            val boolRegister = getInstruction<FiveRegisterInstruction>(shufflingContextCallIndex).registerD

            addInstruction(
                shufflingContextCallIndex,
                "sget-object v$boolRegister, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;"
            )
        }


        val contextMenuViewModelClassDef = contextMenuViewModelClassFingerprint.originalClassDef

        // Hook the method which adds context menu items and return before adding if the item is a Premium ad.
        contextMenuViewModelAddItemFingerprint.match(contextMenuViewModelClassDef).method.apply {
            val contextMenuItemClassType = parameterTypes.first()
            val contextMenuItemClassDef = classBy(contextMenuItemClassType.toString())

            // The class returned by ContextMenuItem->getViewModel, which represents the actual context menu item.
            val viewModelClassType = getViewModelFingerprint.match(contextMenuItemClassDef).originalMethod.returnType

            // The instruction where the normal method logic starts.
            val firstInstruction = getInstruction(0)

            val isFilteredContextMenuItemDescriptor =
                "$EXTENSION_CLASS_DESCRIPTOR->isFilteredContextMenuItem(Ljava/lang/Object;)Z"

            addInstructionsWithLabels(
                0,
                """
                    # The first parameter is the context menu item being added.
                    # Invoke getViewModel to get the actual context menu item.
                    invoke-interface { p1 }, $contextMenuItemClassType->getViewModel()$viewModelClassType
                    move-result-object v0

                    # Check if this context menu item should be filtered out.
                    invoke-static { v0 }, $isFilteredContextMenuItemDescriptor
                    move-result v0

                    # If this context menu item should not be filtered out, jump to the normal method logic.
                    if-eqz v0, :normal-method-logic
                    return-void
                """,
                ExternalLabel("normal-method-logic", firstInstruction)
            )
        }


        val protobufArrayListClassDef = with(protobufListsFingerprint.originalMethod) {
            val emptyProtobufListGetIndex = indexOfFirstInstructionOrThrow(Opcode.SGET_OBJECT)
            // Find the protobuf array list class using the definingClass which contains the empty list static value.
            val classType = getInstruction(emptyProtobufListGetIndex).getReference<FieldReference>()!!.definingClass

            classBy { it.type == classType }
        }

        val abstractProtobufListClassDef = classBy {
            it.type == protobufArrayListClassDef.superclass
        }

        // Need to allow mutation of the list so the home ads sections can be removed.
        // Protobuf array list has an 'isMutable' boolean parameter that sets the mutability.
        // Forcing that always on breaks unrelated code in strange ways.
        // Instead, return early in the method that throws an error if the list is immutable.
        abstractProtobufListEnsureIsMutableFingerprint.match(abstractProtobufListClassDef)
            .method.returnEarly()

        // Make featureTypeCase_ accessible so we can check the home section type in the extension.
        homeSectionFingerprint.classDef.publicizeField("featureTypeCase_")

        // Remove ads sections from home.
        homeStructureGetSectionsFingerprint.method.apply {
            val getSectionsIndex = indexOfFirstInstructionOrThrow(Opcode.IGET_OBJECT)
            val sectionsRegister = getInstruction<TwoRegisterInstruction>(getSectionsIndex).registerA

            addInstruction(
                getSectionsIndex + 1,
                "invoke-static { v$sectionsRegister }, " +
                        "$EXTENSION_CLASS_DESCRIPTOR->removeHomeSections(Ljava/util/List;)V"
            )
        }


        // Replace a fetch request that returns and maps Singles with their static onErrorReturn value.
        fun MutableMethod.replaceFetchRequestSingleWithError(requestClassName: String) {
            // The index of where the request class is being instantiated.
            val requestInstantiationIndex = indexOfFirstInstructionOrThrow {
                getReference<TypeReference>()?.type?.endsWith(requestClassName) == true
            }

            // The index of where the onErrorReturn method is called with the error static value.
            val onErrorReturnCallIndex = indexOfFirstInstructionOrThrow(requestInstantiationIndex) {
                getReference<MethodReference>()?.name == "onErrorReturn"
            }
            val onErrorReturnCallInstruction = getInstruction<FiveRegisterInstruction>(onErrorReturnCallIndex)

            // The error static value register.
            val onErrorReturnValueRegister = onErrorReturnCallInstruction.registerD

            // The index where the error static value starts being constructed.
            // Because the Singles are mapped, the error static value starts being constructed right after the first
            // move-result-object of the map call, before the onErrorReturn method call.
            val onErrorReturnValueConstructionIndex =
                indexOfFirstInstructionReversedOrThrow(onErrorReturnCallIndex, Opcode.MOVE_RESULT_OBJECT) + 1

            val singleClassName = onErrorReturnCallInstruction.getReference<MethodReference>()!!.definingClass
            // The index where the request is firstly called, before its result is mapped to other values.
            val requestCallIndex = indexOfFirstInstructionOrThrow(requestInstantiationIndex) {
                getReference<MethodReference>()?.returnType == singleClassName
            }

            // Construct a new single with the error static value and return it.
            addInstructions(
                onErrorReturnCallIndex,
                "invoke-static { v$onErrorReturnValueRegister }, " +
                        "$singleClassName->just(Ljava/lang/Object;)$singleClassName\n" +
                "move-result-object v$onErrorReturnValueRegister\n" +
                "return-object v$onErrorReturnValueRegister"
            )

            // Remove every instruction from the request call to right before the error static value construction.
            val removeCount = onErrorReturnValueConstructionIndex - requestCallIndex
            removeInstructions(requestCallIndex, removeCount)
        }

        // Remove pendragon (pop up ads) requests and return the errors instead.
        pendragonJsonFetchMessageRequestFingerprint.method.replaceFetchRequestSingleWithError(
            PENDRAGON_JSON_FETCH_MESSAGE_REQUEST_CLASS_NAME
        )
        pendragonProtoFetchMessageListRequestFingerprint.method.replaceFetchRequestSingleWithError(
            PENDRAGON_PROTO_FETCH_MESSAGE_LIST_REQUEST_CLASS_NAME
        )
    }
}
