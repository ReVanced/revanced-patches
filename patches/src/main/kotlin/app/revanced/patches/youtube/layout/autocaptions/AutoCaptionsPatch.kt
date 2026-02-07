package app.revanced.patches.youtube.layout.autocaptions

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.shared.subtitleButtonControllerMethod

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/DisableAutoCaptionsPatch;"

@Suppress("unused")
val disableAutoCaptionsPatch = bytecodePatch(
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
            "20.14.43",
            "20.21.37",
            "20.26.46",
            "20.31.42",
            "20.37.48",
            "20.40.45"
        ),
    )

    apply {
        addResources("youtube", "layout.autocaptions.autoCaptionsPatch")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_disable_auto_captions"),
        )

        subtitleTrackMethod.addInstructions(
            0,
            """
                invoke-static {}, $EXTENSION_CLASS_DESCRIPTOR->disableAutoCaptions()Z
                move-result v0
                if-eqz v0, :auto_captions_enabled
                const/4 v0, 0x1
                return v0
                :auto_captions_enabled
                nop
            """,
        )

        arrayOf(
            startVideoInformerMethod to 0,
            subtitleButtonControllerMethod to 1,
        ).forEach { (method, enabled) ->
            method.addInstructions(
                0,
                """
                    const/4 v0, 0x$enabled
                    invoke-static { v0 }, $EXTENSION_CLASS_DESCRIPTOR->setCaptionsButtonStatus(Z)V
                """,
            )
        }
    }
}
