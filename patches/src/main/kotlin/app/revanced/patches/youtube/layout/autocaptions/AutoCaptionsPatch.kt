package app.revanced.patches.youtube.layout.autocaptions

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.shared.subtitleButtonControllerFingerprint

@Suppress("unused")
val autoCaptionsPatch = bytecodePatch(
    name = "Disable auto captions",
    description = "Adds an option to disable captions from being automatically enabled.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "18.38.44",
            "18.49.37",
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
        ),
    )

    execute {
        addResources("youtube", "layout.autocaptions.autoCaptionsPatch")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_auto_captions"),
        )

        mapOf(
            startVideoInformerFingerprint to 0,
            subtitleButtonControllerFingerprint to 1,
        ).forEach { (fingerprint, enabled) ->
            fingerprint.method().addInstructions(
                0,
                """
                    const/4 v0, 0x$enabled
                    sput-boolean v0, Lapp/revanced/extension/youtube/patches/DisableAutoCaptionsPatch;->captionsButtonDisabled:Z
                """,
            )
        }

        subtitleTrackFingerprint.method().addInstructions(
            0,
            """
                invoke-static {}, Lapp/revanced/extension/youtube/patches/DisableAutoCaptionsPatch;->autoCaptionsEnabled()Z
                move-result v0
                if-eqz v0, :auto_captions_enabled
                sget-boolean v0, Lapp/revanced/extension/youtube/patches/DisableAutoCaptionsPatch;->captionsButtonDisabled:Z
                if-nez v0, :auto_captions_enabled
                const/4 v0, 0x1
                return v0
                :auto_captions_enabled
                nop
            """,
        )
    }
}
