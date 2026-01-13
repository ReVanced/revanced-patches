package app.revanced.patches.strava.media.download

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.strava.misc.extension.sharedExtensionPatch
import app.revanced.util.getReference
import app.revanced.util.writeRegister
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction22c
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

private const val ACTION_CLASS_DESCRIPTOR = "Lcom/strava/bottomsheet/Action;"
private const val MEDIA_CLASS_DESCRIPTOR = "Lcom/strava/photos/data/Media;"
private const val MEDIA_DOWNLOAD_CLASS_DESCRIPTOR = "Lapp/revanced/extension/strava/AddMediaDownloadPatch;"

@Suppress("unused")
val addMediaDownloadPatch = bytecodePatch(
    name = "Add media download",
    description = "Extends the full-screen media viewer menu with items to copy or open their URLs or download them directly."
) {
    compatibleWith("com.strava")

    dependsOn(
        resourceMappingPatch,
        sharedExtensionPatch
    )

    execute {
        val fragmentClass = classBy { it.endsWith("/FullscreenMediaFragment;") }!!.mutableClass

        // region Extend menu of `FullscreenMediaFragment` with actions.

        createAndShowFragmentFingerprint.match(fragmentClass).method.apply {
            val setTrueIndex = instructions.indexOfFirst { instruction ->
                instruction.opcode == Opcode.IPUT_BOOLEAN
            }
            val actionRegistrarRegister = getInstruction<BuilderInstruction22c>(setTrueIndex).registerB
            val actionRegister = instructions.first { instruction ->
                instruction.getReference<TypeReference>()?.type == ACTION_CLASS_DESCRIPTOR
            }.writeRegister!!

            fun addMenuItem(actionId: String, string: String, color: String, drawable: String) = addInstructions(
                setTrueIndex + 1,
                """
                    new-instance v$actionRegister, $ACTION_CLASS_DESCRIPTOR
                    sget v${actionRegister + 1}, $MEDIA_DOWNLOAD_CLASS_DESCRIPTOR->$actionId:I
                    const v${actionRegister + 2}, 0x0
                    const v${actionRegister + 3}, ${resourceMappings["string", string]}
                    const v${actionRegister + 4}, ${resourceMappings["color", color]}
                    const v${actionRegister + 5}, ${resourceMappings["drawable", drawable]}
                    move/from16 v${actionRegister + 6}, v${actionRegister + 4}
                    invoke-direct/range { v$actionRegister .. v${actionRegister + 7} }, $ACTION_CLASS_DESCRIPTOR-><init>(ILjava/lang/String;IIIILjava/io/Serializable;)V
                    invoke-virtual { v$actionRegistrarRegister, v$actionRegister }, Lcom/strava/bottomsheet/a;->a(Lcom/strava/bottomsheet/BottomSheetItem;)V
                """
            )

            addMenuItem("ACTION_COPY_LINK", "copy_link", "core_o3", "actions_link_normal_xsmall")
            addMenuItem("ACTION_OPEN_LINK", "fallback_menu_item_open_in_browser", "core_o3", "actions_link_external_normal_xsmall")
            addMenuItem("ACTION_DOWNLOAD", "download", "core_o3", "actions_download_normal_xsmall")

            // Move media to last parameter of `Action` constructor.
            val getMediaInstruction = instructions.first { instruction ->
                instruction.getReference<FieldReference>()?.type == MEDIA_CLASS_DESCRIPTOR
            }
            addInstruction(
                getMediaInstruction.location.index + 1,
                "move-object/from16 v${actionRegister + 7}, v${getMediaInstruction.writeRegister}"
            )
        }

        // endregion

        // region Handle new actions.

        val actionClass = classes.first { clazz ->
            clazz.type == ACTION_CLASS_DESCRIPTOR
        }
        val actionSerializableField = actionClass.instanceFields.first { field ->
            field.type == "Ljava/io/Serializable;"
        }

        // Handle "copy link" & "open link" & "download" actions.
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
                    invoke-static { v$actionIdRegister, v0 }, $MEDIA_DOWNLOAD_CLASS_DESCRIPTOR->handleAction(I$MEDIA_CLASS_DESCRIPTOR)Z
                    move-result v0
                    return v0
                """,
                ExternalLabel("move", instructions[indexAfterMoveInstruction])
            )
        }

        // endregion
    }
}
