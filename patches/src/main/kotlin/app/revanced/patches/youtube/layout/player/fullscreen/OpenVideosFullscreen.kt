package app.revanced.patches.youtube.layout.player.fullscreen

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstLiteralInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/OpenVideosFullscreen;"

@Suppress("unused")
val openVideosFullscreenPatch = bytecodePatch(
    name = "Open videos fullscreen",
    description = "Adds an option to open videos in portrait full screen mode.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "19.46.42",
        )
    )

    execute {
        openVideosFullscreenPortraitFingerprint.method.apply {
            val constIndex = indexOfFirstLiteralInstructionOrThrow(OPEN_VIDEOS_FULLSCREEN_PORTRAIT_FEATURE_FLAG)
            val resultIndex = indexOfFirstInstructionOrThrow(constIndex, Opcode.MOVE_RESULT)
            val register = getInstruction<OneRegisterInstruction>(resultIndex).registerA

            addInstructions(
                resultIndex + 1,
                """
                    invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->openVideoFullscreenPortrait()Z
                    move-result v$register
                """
            )
        }

        // Add resources and setting last, in case the user force patches an old incompatible version.

        addResources("youtube", "layout.player.fullscreen.openVideosFullscreen")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_open_videos_fullscreen_portrait")
        )
    }
}
