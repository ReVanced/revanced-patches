package app.revanced.patches.spotify.misc

import app.revanced.patcher.Fingerprint
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableClass
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.spotify.misc.extension.sharedExtensionPatch
import app.revanced.util.*
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

internal const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/spotify/misc/UnlockPremiumPatch;"

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
        accountAttributeFingerprint.classDef.publicizeField("value_")

        // Override the attributes map in the getter method.
        productStateProtoGetMapFingerprint.method.apply {
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
                buildQueryParametersFingerprint.stringMatches!!.first().index, Opcode.IF_EQZ
            )

            removeInstruction(addQueryParameterConditionIndex)
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
            val boolRegister = getInstruction<FiveRegisterInstruction>(shufflingContextCallIndex).registerD

            addInstruction(
                shufflingContextCallIndex,
                "sget-object v$boolRegister, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;"
            )
        }


        val contextMenuViewModelClassDef = contextMenuViewModelClassFingerprint.originalClassDef

        // Patch used in versions older than "9.0.60.128".
        // Hook the method which adds context menu items and return before adding if the item is a Premium ad.
        oldContextMenuViewModelAddItemFingerprint.matchOrNull(contextMenuViewModelClassDef)?.method?.apply {
            val contextMenuItemInterfaceName = parameterTypes.first()
            val contextMenuItemInterfaceClassDef = classes.find {
                it.type == contextMenuItemInterfaceName
            } ?: throw PatchException("Could not find context menu item interface.")

            // The class returned by ContextMenuItem->getViewModel, which represents the actual context menu item we
            // need to stringify.
            val viewModelClassType =
                getViewModelFingerprint.match(contextMenuItemInterfaceClassDef).originalMethod.returnType

            // The instruction where the normal method logic starts.
            val firstInstruction = getInstruction(0)

            val isFilteredContextMenuItemDescriptor =
                "$EXTENSION_CLASS_DESCRIPTOR->isFilteredContextMenuItem(Ljava/lang/Object;)Z"

            addInstructionsWithLabels(
                0,
                """
                    # The first parameter is the context menu item being added.
                    # Invoke getViewModel to get the actual context menu item.
                    invoke-interface { p1 }, $contextMenuItemInterfaceName->getViewModel()$viewModelClassType
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

        // Patch for newest versions.
        // Overwrite the context menu items list with a filtered version which does not include items which are
        // Premium ads.
        if (oldContextMenuViewModelAddItemFingerprint.matchOrNull(contextMenuViewModelClassDef) == null) {
            // Replace the placeholder context menu item interface name and the return value of getViewModel to the
            // minified names used at runtime. The instructions need to match the original names so we can call the
            // method in the extension.
            extensionFilterContextMenuItemsFingerprint.method.apply {
                val contextMenuItemInterfaceClassDef = removeAdsContextMenuItemClassFingerprint
                    .originalClassDef
                    .interfaces
                    .firstOrNull()
                    ?.let { interfaceName -> classes.find { it.type == interfaceName } }
                    ?: throw PatchException("Could not find context menu item interface.")

                val contextMenuItemViewModelClassName = getViewModelFingerprint
                    .matchOrNull(contextMenuItemInterfaceClassDef)
                    ?.originalMethod
                    ?.returnType
                    ?: throw PatchException("Could not find context menu item view model class.")

                val castContextMenuItemStubIndex = indexOfFirstInstructionOrThrow {
                    getReference<TypeReference>()?.type == CONTEXT_MENU_ITEM_CLASS_DESCRIPTOR_PLACEHOLDER
                }
                val contextMenuItemRegister = getInstruction<OneRegisterInstruction>(castContextMenuItemStubIndex)
                    .registerA
                val getContextMenuItemStubViewModelIndex = indexOfFirstInstructionOrThrow {
                    getReference<MethodReference>()?.definingClass == CONTEXT_MENU_ITEM_CLASS_DESCRIPTOR_PLACEHOLDER
                }

                val getViewModelDescriptor =
                    "$contextMenuItemInterfaceClassDef->getViewModel()$contextMenuItemViewModelClassName"

                replaceInstruction(
                    castContextMenuItemStubIndex,
                    "check-cast v$contextMenuItemRegister, $contextMenuItemInterfaceClassDef"
                )
                replaceInstruction(
                    getContextMenuItemStubViewModelIndex,
                    "invoke-interface { v$contextMenuItemRegister }, $getViewModelDescriptor"
                )
            }

            contextMenuViewModelConstructorFingerprint.match(contextMenuViewModelClassDef).method.apply {
                val itemsListParameter = parameters.indexOfFirst { it.type == "Ljava/util/List;" } + 1
                val filterContextMenuItemsDescriptor =
                    "$EXTENSION_CLASS_DESCRIPTOR->filterContextMenuItems(Ljava/util/List;)Ljava/util/List;"

                addInstructions(
                    0,
                    """
                        invoke-static { p$itemsListParameter }, $filterContextMenuItemsDescriptor
                        move-result-object p$itemsListParameter
                    """
                )
            }
        }


        val protobufArrayListClassDef = with(protobufListsFingerprint.originalMethod) {
            val emptyProtobufListGetIndex = indexOfFirstInstructionOrThrow(Opcode.SGET_OBJECT)
            // Find the protobuf array list class using the definingClass which contains the empty list static value.
            val classType = getInstruction(emptyProtobufListGetIndex).getReference<FieldReference>()!!.definingClass

            classes.find { it.type == classType } ?: throw PatchException("Could not find protobuf array list class.")
        }

        val abstractProtobufListClassDef = classes.find {
            it.type == protobufArrayListClassDef.superclass
        } ?: throw PatchException("Could not find abstract protobuf list class.")

        // Need to allow mutation of the list so the home ads sections can be removed.
        // Protobuf array list has an 'isMutable' boolean parameter that sets the mutability.
        // Forcing that always on breaks unrelated code in strange ways.
        // Instead, return early in the method that throws an error if the list is immutable.
        abstractProtobufListEnsureIsMutableFingerprint.match(abstractProtobufListClassDef)
            .method.returnEarly()

        fun MutableMethod.injectRemoveSectionCall(
            sectionFingerprint: Fingerprint,
            sectionTypeFieldName: String,
            injectedMethodName: String
        ) {
            // Make field accessible so we can check the home/browse section type in the extension.
            sectionFingerprint.classDef.publicizeField(sectionTypeFieldName)

            val getSectionsIndex = indexOfFirstInstructionOrThrow(Opcode.IGET_OBJECT)
            val sectionsRegister = getInstruction<TwoRegisterInstruction>(getSectionsIndex).registerA

            addInstruction(
                getSectionsIndex + 1,
                "invoke-static { v$sectionsRegister }, " +
                        "$EXTENSION_CLASS_DESCRIPTOR->$injectedMethodName(Ljava/util/List;)V"
            )
        }

        homeStructureGetSectionsFingerprint.method.injectRemoveSectionCall(
            homeSectionFingerprint,
            "featureTypeCase_",
            "removeHomeSections"
        )

        browseStructureGetSectionsFingerprint.method.injectRemoveSectionCall(
            browseSectionFingerprint,
            "sectionTypeCase_",
            "removeBrowseSections"
        )


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
