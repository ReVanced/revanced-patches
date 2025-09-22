package app.revanced.patches.youtube.misc.loopvideo

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.loopvideo.button.loopVideoButtonPatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.shared.autoRepeatFingerprint
import app.revanced.patches.youtube.shared.autoRepeatParentFingerprint

val loopVideoPatch = bytecodePatch(
    name = "Loop video",
    description = "Adds an option to loop videos and display loop video button in the video player.",
) {
    dependsOn(
        sharedExtensionPatch,
        addResourcesPatch,
        loopVideoButtonPatch
    )

    compatibleWith(
        "com.google.android.youtube"(
            "19.34.42",
            "19.43.41",
            "20.07.39",
            "20.13.41",
            "20.14.43",
        )
    )

    execute {
        addResources("youtube", "misc.loopvideo.loopVideoPatch")

        PreferenceScreen.MISC.addPreferences(
            SwitchPreference("revanced_loop_video"),
        )

        autoRepeatFingerprint.match(autoRepeatParentFingerprint.originalClassDef).method.apply {
            val playMethod = autoRepeatParentFingerprint.method
            val index = instructions.lastIndex

            // Remove return-void.
            removeInstruction(index)
            // Add own instructions there.
            addInstructionsWithLabels(
                index,
                """
                    invoke-static {}, Lapp/revanced/extension/youtube/patches/LoopVideoPatch;->shouldLoopVideo()Z
                    move-result v0
                    if-eqz v0, :noautorepeat
                    invoke-virtual { p0 }, $playMethod
                    :noautorepeat
                    return-void
                """,
            )
        }
    }
}
