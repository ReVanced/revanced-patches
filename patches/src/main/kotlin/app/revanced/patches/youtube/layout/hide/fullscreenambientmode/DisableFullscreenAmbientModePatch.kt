package app.revanced.patches.youtube.layout.hide.fullscreenambientmode

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch

internal const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/DisableFullscreenAmbientModePatch;"

@Suppress("unused")
val disableFullscreenAmbientModePatch = bytecodePatch(
    name = "Disable fullscreen ambient mode",
    description = "Adds an option to disable the ambient mode when in fullscreen.",
) {
    dependsOn(
        settingsPatch,
        sharedExtensionPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "18.37.36",
            "18.38.44",
            "18.43.45",
            "18.44.41",
            "18.45.43",
            "18.48.39",
            "18.49.37",
            "19.01.34",
            "19.02.39",
            "19.03.36",
            "19.04.38",
            "19.05.36",
            "19.06.39",
            "19.07.40",
            "19.08.36",
            "19.09.38",
            "19.10.39",
            "19.11.43",
            "19.12.41",
            "19.13.37",
            "19.14.43",
            "19.15.36",
            "19.16.39",
        ),
    )

    val initializeAmbientModeMatch by initializeAmbientModeFingerprint()

    execute {
        addResources("youtube", "layout.hide.fullscreenambientmode.disableFullscreenAmbientModePatch")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_disable_fullscreen_ambient_mode"),
        )

        initializeAmbientModeMatch.mutableMethod.apply {
            val moveIsEnabledIndex = initializeAmbientModeMatch.patternMatch!!.endIndex

            addInstruction(
                moveIsEnabledIndex,
                "invoke-static { }, " +
                    "$EXTENSION_CLASS_DESCRIPTOR->enableFullScreenAmbientMode()Z",
            )
        }
    }
}
