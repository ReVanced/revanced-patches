package app.revanced.patches.youtube.misc.fix.playback.patch

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.shared.mapping.misc.patch.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch

@DependsOn([SettingsPatch::class, ResourceMappingPatch::class])
class SpoofSignatureVerificationResourcePatch : ResourcePatch {

    override fun execute(context: ResourceContext) {
        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            SwitchPreference(
                "revanced_spoof_signature_verification",
                StringResource("revanced_spoof_signature_verification_title", "Spoof app signature"),
                StringResource("revanced_spoof_signature_verification_summary_on",
                    "App signature spoofed\\n\\n"
                        + "Side effects include:\\n"
                        + "• Ambient mode may not work\\n"
                        + "• Downloading videos may not work\\n"
                        + "• Seekbar thumbnails are always hidden"),
                StringResource("revanced_spoof_signature_verification_summary_off", "App signature not spoofed\\n\\nVideo playback may not work"),
                StringResource("revanced_spoof_signature_verification_user_dialog_message",
                    "Turning off this setting will cause video playback issues.")
            )
        )

        scrubbedPreviewThumbnailResourceId = ResourceMappingPatch.resourceMappings.single {
            it.type == "id" && it.name == "thumbnail"
        }.id
    }

    companion object {
        var scrubbedPreviewThumbnailResourceId: Long = -1
    }
}
