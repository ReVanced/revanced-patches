package app.revanced.patches.youtube.misc.gms

import app.revanced.patcher.data.ResourceContext
import app.revanced.patches.shared.misc.gms.BaseGmsCoreSupportResourcePatch
import app.revanced.patches.shared.misc.settings.preference.impl.IntentPreference
import app.revanced.patches.youtube.misc.gms.Constants.REVANCED_YOUTUBE_PACKAGE_NAME
import app.revanced.patches.youtube.misc.gms.Constants.YOUTUBE_PACKAGE_NAME
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.resource.StringResource


object GmsCoreSupportResourcePatch : BaseGmsCoreSupportResourcePatch(
    fromPackageName = YOUTUBE_PACKAGE_NAME,
    toPackageName = REVANCED_YOUTUBE_PACKAGE_NAME,
    spoofedPackageSignature = "24bb24c05e47e0aefa68a58a766179d9b613a600",
    dependencies = setOf(SettingsPatch::class)
) {
    override fun execute(context: ResourceContext) {
        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            IntentPreference(
                StringResource("microg_settings", "GmsCore Settings"),
                StringResource("microg_settings_summary", "Settings for GmsCore"),
                IntentPreference.Intent("", "org.microg.gms.ui.SettingsActivity") {
                    "$gmsCoreVendor.android.gms"
                }
            )
        )

        super.execute(context)
    }
}
