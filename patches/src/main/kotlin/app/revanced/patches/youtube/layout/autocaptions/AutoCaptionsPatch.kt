package app.revanced.patches.youtube.layout.autocaptions

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch

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
            "18.32.39",
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

    val startVideoInformerMatch by startVideoInformerFingerprint()
    val subtitleButtonControllerMatch by subtitleButtonControllerFingerprint()
    val subtitleTrackMatch by subtitleTrackFingerprint()

    execute {
        addResources("youtube", "layout.autocaptions.autoCaptionsPatch")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_auto_captions"),
        )

        mapOf(
            startVideoInformerMatch to 0,
            subtitleButtonControllerMatch to 1,
        ).forEach { (match, enabled) ->
            match.mutableMethod.addInstructions(
                0,
                """
                    const/4 v0, 0x$enabled
                    sput-boolean v0, Lapp/revanced/extension/youtube/patches/DisableAutoCaptionsPatch;->captionsButtonDisabled:Z
                """,
            )
        }

        subtitleTrackMatch.mutableMethod.addInstructions(
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
