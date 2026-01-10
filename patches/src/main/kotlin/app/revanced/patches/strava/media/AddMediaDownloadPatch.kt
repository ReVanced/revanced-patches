package app.revanced.patches.strava.media

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.shared.misc.extension.extensionHook
import app.revanced.patches.shared.misc.extension.sharedExtensionPatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.util.getReference
import app.revanced.util.writeRegister
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction22c
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter

internal const val SET_UTILS_CONTEXT_METHOD_NAME = "setUtilsContext"

private const val ACTION_CLASS_DESCRIPTOR = "Lcom/strava/bottomsheet/Action;"
private const val MEDIA_CLASS_DESCRIPTOR = "Lcom/strava/photos/data/Media;"
private const val VIDEO_CLASS_DESCRIPTOR = "Lcom/strava/photos/data/Media\$Video;"
private const val MEDIA_TYPE_CLASS_DESCRIPTOR = "Lcom/strava/core/data/MediaType;"
private const val MEDIA_DOWNLOAD_CLASS_DESCRIPTOR = "Lapp/revanced/extension/strava/AddMediaDownloadPatch;"

private const val ACTION_DOWNLOAD: Byte = -0x8 // Nibble.MIN_VALUE
private const val ACTION_OPEN_LINK: Byte = -0x7
private const val ACTION_COPY_LINK: Byte = -0x6

@Suppress("unused")
val addMediaDownloadPatch = bytecodePatch(
    name = "Add media download",
    description = "Extends the full-screen media viewer menu with items to copy or open their URLs or download them directly."
) {
    compatibleWith("com.strava")

    dependsOn(
        resourceMappingPatch,
        sharedExtensionPatch(
            "strava",
            extensionHook(
                // Insert before return.
                insertIndexResolver = { method -> method.instructions.toList().size - 1 },
                fingerprint = setUtilsContextFingerprint
            )
        )
    )

    execute {
        val fragmentClass = classBy { it.endsWith("/FullscreenMediaFragment;") }!!.mutableClass

        // Create helper method for setting the `Utils` context.
        val setUtilsContextMethod = ImmutableMethod(
            fragmentClass.type,
            SET_UTILS_CONTEXT_METHOD_NAME,
            listOf(),
            "V",
            AccessFlags.PRIVATE.value,
            setOf(),
            setOf(),
            MutableMethodImplementation(1)
        ).toMutable().apply {
            addInstructions(
                """
                    invoke-virtual { p0 }, Landroidx/fragment/app/Fragment;->requireContext()Landroid/content/Context;
                    move-result-object p0
                    return-void
                """
            )
            fragmentClass.methods.add(this)
        }
        // Eagerly match for extension hook.
        setUtilsContextFingerprint.match(fragmentClass)

        // Extend menu of `FullscreenMediaFragment` with actions.
        run {
            createAndShowFragmentFingerprint.match(fragmentClass).method.apply {
                val setTrueIndex = instructions.indexOfFirst { instruction ->
                    instruction.opcode == Opcode.IPUT_BOOLEAN
                }
                val actionRegistrarRegister = getInstruction<BuilderInstruction22c>(setTrueIndex).registerB
                val actionRegister = instructions.first { instruction ->
                    instruction.opcode == Opcode.NEW_INSTANCE &&
                            instruction.getReference<TypeReference>()!!.type == ACTION_CLASS_DESCRIPTOR
                }.writeRegister!!

                fun addMenuItem(actionId: Byte, string: String, color: String, drawable: String) {
                    addInstructions(
                        setTrueIndex + 1,
                        """
                            new-instance v$actionRegister, $ACTION_CLASS_DESCRIPTOR
                            const v${actionRegister + 1}, $actionId
                            const v${actionRegister + 2}, 0x0
                            const v${actionRegister + 3}, ${resourceMappings["string", string]}
                            const v${actionRegister + 4}, ${resourceMappings["color", color]}
                            const v${actionRegister + 5}, ${resourceMappings["drawable", drawable]}
                            move/from16 v${actionRegister + 6}, v${actionRegister + 4}
                            invoke-direct/range { v$actionRegister .. v${actionRegister + 7} }, $ACTION_CLASS_DESCRIPTOR-><init>(ILjava/lang/String;IIIILjava/io/Serializable;)V
                            invoke-virtual { v$actionRegistrarRegister, v$actionRegister }, Lcom/strava/bottomsheet/a;->a(Lcom/strava/bottomsheet/BottomSheetItem;)V
                        """
                    )
                }

                addMenuItem(ACTION_COPY_LINK, "copy_link", "core_o3", "actions_link_normal_xsmall")
                addMenuItem(ACTION_OPEN_LINK, "fallback_menu_item_open_in_browser", "core_o3", "actions_link_external_normal_xsmall")
                addMenuItem(ACTION_DOWNLOAD, "download", "core_o3", "actions_download_normal_xsmall")

                // Move media to last parameter of `Action` constructor.
                val getMediaInstruction = instructions.first { instruction ->
                    instruction.opcode == Opcode.IGET_OBJECT &&
                            instruction.getReference<FieldReference>()!!.type == MEDIA_CLASS_DESCRIPTOR
                }
                addInstruction(
                    getMediaInstruction.location.index + 1,
                    "move-object/from16 v${actionRegister + 7}, v${getMediaInstruction.writeRegister}"
                )
            }
        }

        // Handle new actions.
        run {
            val actionClass = classes.first { clazz ->
                clazz.type == ACTION_CLASS_DESCRIPTOR
            }
            val actionSerializableField = actionClass.instanceFields.first { field ->
                field.type == "Ljava/io/Serializable;"
            }

            // Handle "copy link" & "open link" & "download" actions.
            val handlerMethod = ImmutableMethod(
                fragmentClass.type,
                "handleCustomAction",
                listOf(
                    ImmutableMethodParameter("I", null, "actionId"),
                    ImmutableMethodParameter(MEDIA_CLASS_DESCRIPTOR, null, "media"),
                ),
                "Z",
                AccessFlags.PRIVATE.value or AccessFlags.STATIC.value,
                null,
                null,
                MutableMethodImplementation(5),
            ).toMutable().apply {
                addInstructions(
                    """
                        invoke-virtual { p1 }, $MEDIA_CLASS_DESCRIPTOR->getType()$MEDIA_TYPE_CLASS_DESCRIPTOR
                        move-result-object v0
                        sget-object v1, $MEDIA_TYPE_CLASS_DESCRIPTOR->PHOTO:$MEDIA_TYPE_CLASS_DESCRIPTOR
                        if-ne v0, v1, :video
                        const/4 v2, 0x1 # isPhoto
                        invoke-virtual { p1 }, $MEDIA_CLASS_DESCRIPTOR->getLargestUrl()Ljava/lang/String;
                        move-result-object v0
                        goto :switch_action
                        :video
                        const/4 v2, 0x0 # !isPhoto
                        sget-object v1, $MEDIA_TYPE_CLASS_DESCRIPTOR->VIDEO:$MEDIA_TYPE_CLASS_DESCRIPTOR
                        if-ne v0, v1, :failure
                        check-cast p1, $VIDEO_CLASS_DESCRIPTOR
                        invoke-virtual { p1 }, $VIDEO_CLASS_DESCRIPTOR->getVideoUrl()Ljava/lang/String;
                        move-result-object v0
                        :switch_action
                        packed-switch p0, :switch_data
                        const/4 v0, 0x0
                        return v0
                        :download
                        invoke-virtual { p1 }, $MEDIA_CLASS_DESCRIPTOR->getId()Ljava/lang/String;
                        move-result-object v1
                        if-eqz v2, :download_video
                        invoke-static { v0, v1 }, $MEDIA_DOWNLOAD_CLASS_DESCRIPTOR->photo(Ljava/lang/String;Ljava/lang/String;)V
                        goto :success
                        :download_video
                        invoke-static { v0, v1 }, $MEDIA_DOWNLOAD_CLASS_DESCRIPTOR->video(Ljava/lang/String;Ljava/lang/String;)V
                        goto :success
                        :open_link
                        invoke-static { v0 }, Lapp/revanced/extension/shared/Utils;->openLink(Ljava/lang/String;)V
                        goto :success
                        :copy_link
                        invoke-static { v0 }, $MEDIA_DOWNLOAD_CLASS_DESCRIPTOR->copyLink(Ljava/lang/CharSequence;)V
                        :success
                        const/4 v0, 0x1
                        return v0
                        :failure
                        const/4 v0, 0x0
                        return v0
                        :switch_data
                        .packed-switch $ACTION_DOWNLOAD
                            :download
                            :open_link
                            :copy_link
                        .end packed-switch
                    """
                )
            }
            fragmentClass.methods.add(handlerMethod)

            handleMediaActionFingerprint.match(fragmentClass).method.apply {
                // Call handler if action ID < 0 (= custom).
                val moveInstruction = instructions.first { instruction ->
                    instruction.opcode == Opcode.MOVE_RESULT
                }
                val indexAfterMoveInstruction = moveInstruction.location.index + 1
                val actionIdRegister = moveInstruction.writeRegister
                addInstructionsWithLabels(
                    indexAfterMoveInstruction,
                    """
                        if-gez v$actionIdRegister, :move
                        check-cast p2, $ACTION_CLASS_DESCRIPTOR
                        iget-object v0, p2, $actionSerializableField
                        check-cast v0, $MEDIA_CLASS_DESCRIPTOR
                        invoke-static { v$actionIdRegister, v0 }, $handlerMethod
                        move-result v0
                        return v0
                    """,
                    ExternalLabel("move", instructions[indexAfterMoveInstruction])
                )

                addInstruction(0, "invoke-direct/range { p0 .. p0 }, $setUtilsContextMethod")
            }
        }
    }
}
