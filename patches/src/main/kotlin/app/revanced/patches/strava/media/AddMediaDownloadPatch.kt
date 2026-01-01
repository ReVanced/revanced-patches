package app.revanced.patches.strava.media

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.shared.misc.extension.extensionHook
import app.revanced.patches.shared.misc.extension.sharedExtensionPatch
import app.revanced.util.getReference
import app.revanced.util.registersUsed
import app.revanced.util.writeRegister
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

private const val ACTION = "Lcom/strava/bottomsheet/Action;"
private const val MEDIA = "Lcom/strava/photos/data/Media;"
private const val RESOURCES = "Lapp/revanced/extension/strava/Resources"

@Suppress("unused")
val addMediaDownloadPatch = bytecodePatch(
    name = "Add media download",
) {
    compatibleWith("com.strava")

    dependsOn(
        sharedExtensionPatch(
            "strava",
            extensionHook(
                insertIndexResolver = { method ->
                    method.instructions.indexOfFirst { it.opcode == Opcode.MOVE_RESULT_OBJECT } + 1
                },
                fingerprint = createAndShowFragmentFingerprint
            )
        )
    )

    execute {
        // extend menu of `FullscreenMediaFragment` with actions
        run {
            createAndShowFragmentFingerprint.match(handleMediaActionFingerprint.originalClassDef).method.apply {
                // add menu item for "copy link" action
                val setTrueIndex = instructions.indexOfFirst { instruction ->
                    instruction.opcode == Opcode.IPUT_BOOLEAN
                }
                addInstructions(
                    setTrueIndex + 1,
                    """
                        const v13, -0x1
                        new-instance v12, $ACTION
                        const v14, 0x0
                        invoke-static { }, $RESOURCES${'$'}Strings;->copyLink()I
                        move-result v15
                        invoke-static { }, $RESOURCES${'$'}Colors;->coreAsphalt()I
                        move-result v16
                        invoke-static { }, $RESOURCES${'$'}Drawables;->actionsLinkNormalXsmall()I
                        move-result v17
                        const v18, 0x0
                        invoke-direct/range {v12 .. v19}, $ACTION-><init>(ILjava/lang/String;IIIILjava/io/Serializable;)V
                        invoke-virtual {v11, v12}, Lcom/strava/bottomsheet/a;->a(Lcom/strava/bottomsheet/BottomSheetItem;)V
                    """
                )

                // move media to last parameter of `Action` constructor
                val getMedia = instructions.first { instruction ->
                    instruction.opcode == Opcode.IGET_OBJECT && instruction.getReference<FieldReference>()!!.type == MEDIA
                }
                addInstruction(getMedia.location.index + 1, "move-object/from16 v19, v${getMedia.writeRegister}")

                // overwrite `this` with context for `Utils`
                val readThisIndex = instructions.indexOfFirst { instruction ->
                    val registersUsed = instruction.registersUsed
                    registersUsed.isNotEmpty() &&
                            registersUsed.last() == implementation!!.registerCount - parameters.size - 1
                }
                addInstructions(
                    readThisIndex + 1,
                    """
                        invoke-virtual/range { p0 .. p0 }, Landroidx/fragment/app/Fragment;->requireContext()Landroid/content/Context;
                        move-result-object p0
                    """
                )
            }
        }

        // handle new actions
        run {
            val actionClass = classes.first { clazz ->
                clazz.type == ACTION
            }
            val actionSerializableField = actionClass.instanceFields.first { field ->
                field.type == "Ljava/io/Serializable;"
            }

            handleMediaActionFingerprint.method.apply {
                // handle "copy link" action
                val move = instructions.first { instruction ->
                    instruction.opcode == Opcode.MOVE_RESULT
                }
                val indexAfterMove = move.location.index + 1
                val actionId = move.writeRegister
                addInstructionsWithLabels(
                    indexAfterMove,
                    """
                        const/4 v0, -0x1
                        if-ne v$actionId, v0, :move
                        check-cast p2, $ACTION
                        iget-object v0, p2, $actionSerializableField
                        check-cast v0, $MEDIA
                        invoke-virtual { v0 }, $MEDIA->getLargestUrl()Ljava/lang/String;
                        move-result-object v0
                        invoke-static { v0 }, Lapp/revanced/extension/shared/Utils;->setClipboard(Ljava/lang/CharSequence;)V
                        const/4 v0, 0x1
                        return v0
                    """,
                    ExternalLabel("move", instructions[indexAfterMove])
                )
            }
        }
    }
}
