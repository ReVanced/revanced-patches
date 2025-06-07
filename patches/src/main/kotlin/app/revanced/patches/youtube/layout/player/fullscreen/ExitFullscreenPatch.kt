package app.revanced.patches.youtube.layout.player.fullscreen

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playercontrols.playerControlsPatch
import app.revanced.patches.youtube.misc.playertype.playerTypeHookPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.shared.autoRepeatFingerprint
import app.revanced.patches.youtube.shared.autoRepeatParentFingerprint
import app.revanced.util.addInstructionsAtControlFlowLabel

@Suppress("unused")
internal val exitFullscreenPatch = bytecodePatch(
    name = "Exit fullscreen mode",
    description = "Adds options to automatically exit fullscreen mode when a video reaches the end."
) {

    compatibleWith(
        "com.google.android.youtube"(
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
            "19.47.53",
            "20.07.39",
            "20.12.46",
        )
    )

    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
        playerTypeHookPatch,
        playerControlsPatch
    )

    // Cannot declare as top level since this patch is in the same package as
    // other patches that declare same constant name with internal visibility.
    @Suppress("LocalVariableName")
    val EXTENSION_CLASS_DESCRIPTOR =
        "Lapp/revanced/extension/youtube/patches/ExitFullscreenPatch;"

    execute {
        addResources("youtube", "layout.player.fullscreen.exitFullscreenPatch")

        PreferenceScreen.PLAYER.addPreferences(
            ListPreference(
                "revanced_exit_fullscreen",
                summaryKey = null,
                tag = "app.revanced.extension.youtube.settings.preference.CustomDialogListPreference"
            )
        )

        autoRepeatFingerprint.match(autoRepeatParentFingerprint.originalClassDef).method.apply {
            addInstructionsAtControlFlowLabel(
                implementation!!.instructions.lastIndex,
                "invoke-static {}, $EXTENSION_CLASS_DESCRIPTOR->endOfVideoReached()V",
            )
        }
    }
}
