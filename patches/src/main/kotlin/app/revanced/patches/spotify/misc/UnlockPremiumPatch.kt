package app.revanced.patches.spotify.misc

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.spotify.misc.extension.IS_SPOTIFY_LEGACY_APP_TARGET
import app.revanced.patches.spotify.misc.extension.sharedExtensionPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import java.util.logging.Logger

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/spotify/misc/UnlockPremiumPatch;"

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Spotify Premium",
    description = "Unlocks Spotify Premium features. Server-sided features like downloading songs are still locked.",
) {
    compatibleWith("com.spotify.music")

    dependsOn(
        sharedExtensionPatch,
        // Currently there is no easy way to make a mandatory patch,
        // so for now this is a dependent of this patch.
        //
        // FIXME: Modifying string resources (such as adding patch strings)
        //  is currently failing with ReVanced manager.
        // checkEnvironmentPatch,
    )

    execute {
        // Make _value accessible so that it can be overridden in the extension.
        accountAttributeFingerprint.classDef.fields.first { it.name == "value_" }.apply {
            // Add public flag and remove private.
            accessFlags = accessFlags.or(AccessFlags.PUBLIC.value).and(AccessFlags.PRIVATE.value.inv())
        }

        // Override the attributes map in the getter method.
        productStateProtoGetMapFingerprint.method.apply {
            val getAttributesMapIndex = indexOfFirstInstructionOrThrow(Opcode.IGET_OBJECT)
            val attributesMapRegister = getInstruction<TwoRegisterInstruction>(getAttributesMapIndex).registerA

            addInstruction(
                getAttributesMapIndex + 1,
                "invoke-static { v$attributesMapRegister }, " +
                        "$EXTENSION_CLASS_DESCRIPTOR->overrideAttribute(Ljava/util/Map;)V"
            )
        }


        // Add the query parameter trackRows to show popular tracks in the artist page.
        buildQueryParametersFingerprint.apply {
            val addQueryParameterConditionIndex = method.indexOfFirstInstructionReversedOrThrow(
                stringMatches!!.first().index, Opcode.IF_EQZ
            )

            method.replaceInstruction(addQueryParameterConditionIndex, "nop")
        }


        if (IS_SPOTIFY_LEGACY_APP_TARGET) {
            return@execute Logger.getLogger(this::class.java.name).warning(
                "Patching a legacy Spotify version. Patch functionality may be limited."
            )
        }


        // Enable choosing a specific song/artist via Google Assistant.
        contextFromJsonFingerprint.method.apply {
            val insertIndex = contextFromJsonFingerprint.patternMatch!!.startIndex
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

            val registerBool = getInstruction<FiveRegisterInstruction>(shufflingContextCallIndex).registerD
            addInstruction(
                shufflingContextCallIndex,
                "sget-object v$registerBool, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;"
            )
        }


        // Disable the "Spotify Premium" upsell experiment in context menus.
        contextMenuExperimentsFingerprint.apply {
            val moveIsEnabledIndex = method.indexOfFirstInstructionOrThrow(
                stringMatches!!.first().index, Opcode.MOVE_RESULT
            )
            val isUpsellEnabledRegister = method.getInstruction<OneRegisterInstruction>(moveIsEnabledIndex).registerA

            method.replaceInstruction(moveIsEnabledIndex, "const/4 v$isUpsellEnabledRegister, 0")
        }


        val protobufListClassName = with(protobufListsFingerprint.originalMethod) {
            val emptyProtobufListGetIndex = indexOfFirstInstructionOrThrow(Opcode.SGET_OBJECT)
            getInstruction(emptyProtobufListGetIndex).getReference<FieldReference>()!!.definingClass
        }

        val protobufListRemoveFingerprint = fingerprint {
            custom { method, classDef ->
                method.name == "remove" && classDef.type == protobufListClassName
            }
        }

        // Need to allow mutation of the list so the home ads sections and pendragon messages/triggers can be removed.
        // Protobuffer list has an 'isMutable' boolean parameter that sets the mutability.
        // Forcing that always on breaks unrelated code in strange ways.
        // Instead, remove the method call that checks if the list is unmodifiable.
        protobufListRemoveFingerprint.method.apply {
            val invokeThrowUnmodifiableIndex = indexOfFirstInstructionOrThrow {
                val reference = getReference<MethodReference>()
                opcode == Opcode.INVOKE_VIRTUAL &&
                        reference?.returnType == "V" && reference.parameterTypes.isEmpty()
            }

            // Remove the method call that throws an exception if the list is not mutable.
            removeInstruction(invokeThrowUnmodifiableIndex)
        }

        fun MutableMethod.addModifyProtobufListInstruction(methodName: String) {
            val getListIndex = indexOfFirstInstructionOrThrow(Opcode.IGET_OBJECT)
            val listRegister = getInstruction<TwoRegisterInstruction>(getListIndex).registerA

            addInstruction(
                getListIndex + 1,
                "invoke-static { v$listRegister }, " +
                        "$EXTENSION_CLASS_DESCRIPTOR->$methodName(Ljava/util/List;)V"
            )
        }


        // Make featureTypeCase_ accessible so we can check the home section type in the extension.
        homeSectionFingerprint.classDef.fields.first { it.name == "featureTypeCase_" }.apply {
            // Add public flag and remove private.
            accessFlags = accessFlags.or(AccessFlags.PUBLIC.value).and(AccessFlags.PRIVATE.value.inv())
        }

        // Remove ads sections from home.
        homeStructureGetSectionsFingerprint.method.addModifyProtobufListInstruction("removeHomeSections")


        val pendragonFetchMessageListReponseClassDef =
            classBy { it.type == PENDRAGON_PROTO_FETCH_MESSAGE_LIST_RESPONSE_CLASS_NAME }!!.immutableClass

        // Clear pendragon (pop out ads) messages and triggers from proto response.
        listOf(getMessagesFingerprint, getTriggersFingerprint).forEach { fingerprint ->
            fingerprint.match(pendragonFetchMessageListReponseClassDef)
                .method.addModifyProtobufListInstruction("clearPendragonMessagesOrTriggers")
        }

        // Nullify pendragon InAppMessage from JSON response.
        pendragonJsonFetchMessageResponseConstructorFingerprint.method.addInstruction(
            0,
            "const/4 p1, 0"
        )
    }
}
