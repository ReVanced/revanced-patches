package app.revanced.patches.youtube.misc.loopvideo

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.loopvideo.button.loopVideoButtonPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.shared.loopVideoFingerprint
import app.revanced.patches.youtube.shared.loopVideoParentFingerprint
import app.revanced.util.addInstructionsAtControlFlowLabel
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import com.android.tools.smali.dexlib2.Opcode

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/LoopVideoPatch;"

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

        loopVideoFingerprint.match(loopVideoParentFingerprint.originalClassDef).method.apply {
            val playMethod = loopVideoParentFingerprint.method
            val insertIndex = indexOfFirstInstructionReversedOrThrow(Opcode.RETURN_VOID)

            addInstructionsAtControlFlowLabel(
                insertIndex,
                """
                    invoke-static {}, $EXTENSION_CLASS_DESCRIPTOR->shouldLoopVideo()Z
                    move-result v0
                    if-eqz v0, :do_not_loop
                    invoke-virtual { p0 }, $playMethod
                    :do_not_loop
                    nop
                """
            )
        }
    }
}
