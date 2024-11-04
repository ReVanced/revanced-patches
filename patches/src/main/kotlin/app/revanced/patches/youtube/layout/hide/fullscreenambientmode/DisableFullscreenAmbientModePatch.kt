package app.revanced.patches.youtube.layout.hide.fullscreenambientmode

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.is_19_43_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import java.util.logging.Logger

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
        versionCheckPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "18.38.44",
            "18.49.37",
            "19.16.39",
            "19.25.37",
            "19.34.42",
        ),
    )

    execute {
        // TODO: fix this patch when 19.43+ is eventually supported.
        if (is_19_43_or_greater) {
            // 19.43+ the feature flag was inlined as false and no longer exists.
            // This patch can be updated to change a single method, but for now show a more descriptive error.
            return@execute Logger.getLogger(this::class.java.name)
                .severe("'Disable fullscreen ambient mode' does not yet support 19.43+")
        }

        addResources("youtube", "layout.hide.fullscreenambientmode.disableFullscreenAmbientModePatch")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_disable_fullscreen_ambient_mode"),
        )

        val initializeAmbientModeMatch by initializeAmbientModeFingerprint

        initializeAmbientModeMatch.method.apply {
            val moveIsEnabledIndex = initializeAmbientModeMatch.patternMatch!!.endIndex

            addInstruction(
                moveIsEnabledIndex,
                "invoke-static { }, " +
                    "$EXTENSION_CLASS_DESCRIPTOR->enableFullScreenAmbientMode()Z",
            )
        }
    }
}
