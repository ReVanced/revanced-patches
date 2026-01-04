package app.revanced.patches.strava.media

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
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
import app.revanced.util.registersUsed
import app.revanced.util.writeRegister
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter

private const val ACTION = "Lcom/strava/bottomsheet/Action;"
private const val MEDIA = "Lcom/strava/photos/data/Media;"
private const val VIDEO = "Lcom/strava/photos/data/Media\$Video;"
private const val MEDIA_TYPE = "Lcom/strava/core/data/MediaType;"
private const val MEDIA_DOWNLOAD = "Lapp/revanced/extension/strava/MediaDownload;"

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
                insertIndexResolver = { method ->
                    method.instructions.indexOfFirst { it.opcode == Opcode.MOVE_RESULT_OBJECT } + 1
                },
                fingerprint = createAndShowFragmentFingerprint
            )
        )
    )

    execute {
        val fragment = handleMediaActionFingerprint.originalClassDef

        // extend menu of `FullscreenMediaFragment` with actions
        run {
            createAndShowFragmentFingerprint.match(fragment).method.apply {
                val indexAfterSetTrue = instructions.indexOfFirst { instruction ->
                    instruction.opcode == Opcode.IPUT_BOOLEAN
                } + 1

                fun addMenuItem(actionId: Byte, string: String, color: String, drawable: String) {
                    addInstructions(
                        indexAfterSetTrue,
                        """
                            const/4 v13, $actionId
                            new-instance v12, $ACTION
                            const/4 v14, 0x0
                            const v15, ${resourceMappings["string", string]}
                            const v16, ${resourceMappings["color", color]}
                            const v17, ${resourceMappings["drawable", drawable]}
                            move/from16 v18, v16
                            invoke-direct/range {v12 .. v19}, $ACTION-><init>(ILjava/lang/String;IIIILjava/io/Serializable;)V
                            invoke-virtual {v11, v12}, Lcom/strava/bottomsheet/a;->a(Lcom/strava/bottomsheet/BottomSheetItem;)V
                        """
                    )
                }

                addMenuItem(ACTION_COPY_LINK, "copy_link", "core_o3", "actions_link_normal_xsmall")
                addMenuItem(ACTION_OPEN_LINK, "fallback_menu_item_open_in_browser", "core_o3", "actions_link_external_normal_xsmall")
                addMenuItem(ACTION_DOWNLOAD, "download", "core_o3", "actions_download_normal_xsmall")

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

            // handle "copy link" & "open link" & "download" actions
            val handler = ImmutableMethod(
                fragment.type,
                "handleCustomAction",
                listOf(
                    ImmutableMethodParameter("I", null, "actionId"),
                    ImmutableMethodParameter(MEDIA, null, "media"),
                ),
                "Z",
                AccessFlags.PRIVATE.value or AccessFlags.STATIC.value,
                null,
                null,
                MutableMethodImplementation(5),
            ).toMutable().apply {
                addInstructions(
                    """
                        invoke-virtual { p1 }, $MEDIA->getType()$MEDIA_TYPE
                        move-result-object v0
                        sget-object v1, $MEDIA_TYPE->PHOTO:$MEDIA_TYPE
                        if-ne v0, v1, :video
                        const/4 v2, 0x1 # isPhoto
                        invoke-virtual { p1 }, $MEDIA->getLargestUrl()Ljava/lang/String;
                        move-result-object v0
                        goto :switch_action
                        :video
                        const/4 v2, 0x0 # !isPhoto
                        sget-object v1, $MEDIA_TYPE->VIDEO:$MEDIA_TYPE
                        if-ne v0, v1, :failure
                        check-cast p1, $VIDEO
                        invoke-virtual { p1 }, $VIDEO->getVideoUrl()Ljava/lang/String;
                        move-result-object v0
                        :switch_action
                        packed-switch p0, :switch_data
                        const/4 v0, 0x0
                        return v0
                        :download
                        invoke-virtual { p1 }, $MEDIA->getId()Ljava/lang/String;
                        move-result-object v1
                        if-eqz v2, :download_video
                        invoke-static { v0, v1 }, $MEDIA_DOWNLOAD->photo(Ljava/lang/String;Ljava/lang/String;)V
                        goto :success
                        :download_video
                        invoke-static { v0, v1 }, $MEDIA_DOWNLOAD->video(Ljava/lang/String;Ljava/lang/String;)V
                        goto :success
                        :open_link
                        invoke-static { v0 }, Lapp/revanced/extension/shared/Utils;->openLink(Ljava/lang/String;)V
                        goto :success
                        :copy_link
                        invoke-static { v0 }, $MEDIA_DOWNLOAD->copyLink(Ljava/lang/CharSequence;)V
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
            handleMediaActionFingerprint.classDef.methods.add(handler)

            handleMediaActionFingerprint.method.apply {
                // call handler if action ID < 0 (= custom)
                val move = instructions.first { instruction ->
                    instruction.opcode == Opcode.MOVE_RESULT
                }
                val indexAfterMove = move.location.index + 1
                val actionId = move.writeRegister
                addInstructionsWithLabels(
                    indexAfterMove,
                    """
                        if-gez v$actionId, :move
                        check-cast p2, $ACTION
                        iget-object v0, p2, $actionSerializableField
                        check-cast v0, $MEDIA
                        invoke-static { v$actionId, v0 }, $handler
                        move-result v0
                        return v0
                    """,
                    ExternalLabel("move", instructions[indexAfterMove])
                )
            }
        }
    }
}
