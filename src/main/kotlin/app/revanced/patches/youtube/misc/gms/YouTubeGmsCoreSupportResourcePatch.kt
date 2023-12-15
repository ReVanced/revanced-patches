package app.revanced.patches.youtube.misc.gms

import app.revanced.patcher.data.ResourceContext
import app.revanced.patches.all.misc.packagename.ChangePackageNamePatch
import app.revanced.patches.shared.misc.gms.AbstractGmsCoreSupportResourcePatch
import app.revanced.patches.shared.settings.preference.impl.Preference
import app.revanced.patches.youtube.misc.gms.Constants.REVANCED_YOUTUBE_PACKAGE_NAME
import app.revanced.patches.youtube.misc.gms.Constants.YOUTUBE_PACKAGE_NAME
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.patches.youtube.misc.strings.StringsPatch


object YouTubeGmsCoreSupportResourcePatch : AbstractGmsCoreSupportResourcePatch(
    fromPackageName = YOUTUBE_PACKAGE_NAME,
    toPackageName = REVANCED_YOUTUBE_PACKAGE_NAME,
    spoofedPackageSignature = "24bb24c05e47e0aefa68a58a766179d9b613a600",
    dependencies = setOf(SettingsPatch::class, StringsPatch::class)
) {
    override fun execute(context: ResourceContext) {
        // Strings used by different target apps.
        StringsPatch.includeSharedPatchStrings("GmsCoreSupport");

        // YouTube specific strings.
        StringsPatch.includePatchStrings("YouTubeGmsCoreSupport");

        SettingsPatch.addPreference(
            Preference(
                "revanced_gms_core_settings_title",
                "revanced_gms_core_settings_summary",
                Preference.Intent("$gmsCoreVendor.android.gms", "", "org.microg.gms.ui.SettingsActivity")
            )
        )

        val packageName = ChangePackageNamePatch.setOrGetFallbackPackageName(REVANCED_YOUTUBE_PACKAGE_NAME)
        SettingsPatch.renameIntentsTargetPackage(packageName)

        super.execute(context)
    }
}