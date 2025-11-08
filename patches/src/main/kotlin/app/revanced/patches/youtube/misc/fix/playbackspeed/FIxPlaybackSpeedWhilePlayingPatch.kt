package app.revanced.patches.youtube.misc.fix.playbackspeed

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playertype.playerTypeHookPatch
import app.revanced.patches.youtube.misc.playservice.is_19_34_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.util.findFreeRegister
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/FixPlaybackSpeedWhilePlayingPatch;"

/**
 * Fixes a bug in YouTube 19.34+ where the playback speed
 * can incorrectly reset to 1.0x under certain conditions.
 *
 * Reproduction steps using 19.34+
 * 1. Open a video and start playback
 * 2. Change the speed to any value that is not 1.0x.
 * 3. Open the comments panel.
 * 4. Tap any "N more replies" link at the bottom of a comment, or tap on a timestamp of a comment.
 * 5. Pause the video
 * 6. Resume the video
 * 7. Playback speed will incorrectly change to 1.0x.
 */
val fixPlaybackSpeedWhilePlayingPatch = bytecodePatch{
    dependsOn(
        sharedExtensionPatch,
        playerTypeHookPatch,
        versionCheckPatch,
    )

    execute {
        if (!is_19_34_or_greater) {
            return@execute
        }

        playbackSpeedInFeedsFingerprint.method.apply {
            val playbackSpeedIndex = indexOfGetPlaybackSpeedInstruction(this)
            val playbackSpeedRegister = getInstruction<TwoRegisterInstruction>(playbackSpeedIndex).registerA
            val returnIndex = indexOfFirstInstructionOrThrow(playbackSpeedIndex, Opcode.RETURN_VOID)
            val insertIndex = playbackSpeedIndex + 1
            val freeRegister = findFreeRegister(insertIndex, playbackSpeedRegister)

            addInstructionsWithLabels(
                insertIndex,
                """
                    invoke-static { v$playbackSpeedRegister }, $EXTENSION_CLASS_DESCRIPTOR->playbackSpeedChanged(F)Z
                    move-result v$freeRegister
                    if-nez v$freeRegister, :do_not_change
                """,
                ExternalLabel("do_not_change", getInstruction(returnIndex))
            )
        }
    }
}