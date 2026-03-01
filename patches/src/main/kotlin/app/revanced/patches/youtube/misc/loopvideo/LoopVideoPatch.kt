package app.revanced.patches.youtube.misc.loopvideo

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.loopvideo.button.loopVideoButtonPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.video.information.playerStatusMethod
import app.revanced.patches.youtube.video.information.videoInformationPatch
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/LoopVideoPatch;"

val loopVideoPatch = bytecodePatch(
    name = "Loop video",
    description = "Adds an option to loop videos and display loop video button in the video player.",
) {
    dependsOn(
        sharedExtensionPatch,
        addResourcesPatch,
        loopVideoButtonPatch,
        videoInformationPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "20.14.43",
            "20.21.37",
            "20.26.46",
            "20.31.42",
            "20.37.48",
            "20.40.45"
        ),
    )

    apply {
        addResources("youtube", "misc.loopvideo.loopVideoPatch")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_loop_video"),
        )

        playerStatusMethod.apply {
            // Add call to start playback again, but must happen before "Exit fullscreen" patch call.
            val insertIndex = indexOfFirstInstructionOrThrow(Opcode.SGET_OBJECT)
            // Since instructions are added just above Opcode.SGET_OBJECT, instead of calling findFreeRegister(),
            // a register from Opcode.SGET_OBJECT is used.
            val freeRegister =
                getInstruction<OneRegisterInstruction>(insertIndex).registerA

            // Since 'videoInformationPatch' is used as a dependency of this patch,
            // the loop is implemented through 'VideoInformation.seekTo(0)'.
            addInstructionsWithLabels(
                insertIndex,
                """
                    invoke-static/range { p1 .. p1 }, $EXTENSION_CLASS_DESCRIPTOR->shouldLoopVideo(Ljava/lang/Enum;)Z
                    move-result v$freeRegister
                    if-eqz v$freeRegister, :do_not_loop
                    return-void
                    :do_not_loop
                    nop
                """
            )
        }
    }
}
