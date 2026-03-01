package app.revanced.patches.youtube.layout.player.fullscreen

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playercontrols.playerControlsPatch
import app.revanced.patches.youtube.misc.playertype.playerTypeHookPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.video.information.playerStatusMethod
import app.revanced.patches.youtube.video.information.videoInformationPatch
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode

@Suppress("unused")
val exitFullscreenPatch = bytecodePatch(
    name = "Exit fullscreen",
    description = "Adds options to automatically exit fullscreen mode when a video reaches the end.",
) {
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

    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
        playerTypeHookPatch,
        playerControlsPatch,
        videoInformationPatch,
    )

    // Cannot declare as top level since this patch is in the same package as
    // other patches that declare same constant name with internal visibility.
    @Suppress("LocalVariableName")
    val EXTENSION_CLASS_DESCRIPTOR =
        "Lapp/revanced/extension/youtube/patches/ExitFullscreenPatch;"

    apply {
        addResources("youtube", "layout.player.fullscreen.exitFullscreenPatch")

        PreferenceScreen.PLAYER.addPreferences(
            ListPreference("revanced_exit_fullscreen"),
        )

        playerStatusMethod.apply {
            // +1 to ensure inserted after the loop logic added by the "Loop video" patch.
            val insertIndex = indexOfFirstInstructionOrThrow(Opcode.SGET_OBJECT) + 1

            addInstruction(
                insertIndex,
                "invoke-static/range { p1 .. p1 }, " +
                        "$EXTENSION_CLASS_DESCRIPTOR->endOfVideoReached(Ljava/lang/Enum;)V",
                )
        }
    }
}
