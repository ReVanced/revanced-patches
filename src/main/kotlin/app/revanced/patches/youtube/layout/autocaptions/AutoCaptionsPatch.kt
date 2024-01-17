package app.revanced.patches.youtube.layout.autocaptions

import app.revanced.util.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.layout.autocaptions.fingerprints.StartVideoInformerFingerprint
import app.revanced.patches.youtube.layout.autocaptions.fingerprints.SubtitleButtonControllerFingerprint
import app.revanced.patches.youtube.layout.autocaptions.fingerprints.SubtitleTrackFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch


@Patch(
    name = "Disable auto captions",
    description = "Adds an option to disable captions from being automatically enabled.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.32.39",
                "18.37.36",
                "18.38.44",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.34"
            ]
        )
    ],
)
@Suppress("unused")
object AutoCaptionsPatch : BytecodePatch(
    setOf(StartVideoInformerFingerprint, SubtitleButtonControllerFingerprint, SubtitleTrackFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_auto_captions",
                StringResource("revanced_auto_captions_title", "Disable auto captions"),
                StringResource("revanced_auto_captions_summary_on", "Auto captions are disabled"),
                StringResource("revanced_auto_captions_summary_off", "Auto captions are enabled")
            )
        )

        mapOf(
            StartVideoInformerFingerprint to 0,
            SubtitleButtonControllerFingerprint to 1
        ).forEach { (fingerprint, enabled) ->
            fingerprint.result?.mutableMethod?.addInstructions(
                0,
                """
                    const/4 v0, 0x$enabled
                    sput-boolean v0, Lapp/revanced/integrations/youtube/patches/DisableAutoCaptionsPatch;->captionsButtonDisabled:Z
                """
            ) ?: throw fingerprint.exception
        }

        SubtitleTrackFingerprint.result?.mutableMethod?.addInstructionsWithLabels(
            0,
            """
                invoke-static {}, Lapp/revanced/integrations/youtube/patches/DisableAutoCaptionsPatch;->autoCaptionsEnabled()Z
                move-result v0
                if-eqz v0, :auto_captions_enabled
                sget-boolean v0, Lapp/revanced/integrations/youtube/patches/DisableAutoCaptionsPatch;->captionsButtonDisabled:Z
                if-nez v0, :auto_captions_enabled
                const/4 v0, 0x1
                return v0
                :auto_captions_enabled
                nop
            """
        )
    }
}
