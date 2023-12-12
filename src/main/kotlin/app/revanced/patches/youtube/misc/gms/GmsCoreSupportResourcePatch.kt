package app.revanced.patches.youtube.misc.gms

import app.revanced.patcher.data.ResourceContext
import app.revanced.patches.all.misc.packagename.ChangePackageNamePatch
import app.revanced.patches.shared.misc.gms.AbstractGmsCoreSupportResourcePatch
import app.revanced.patches.shared.settings.preference.impl.Preference
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.youtube.misc.gms.Constants.REVANCED_YOUTUBE_PACKAGE_NAME
import app.revanced.patches.youtube.misc.gms.Constants.YOUTUBE_PACKAGE_NAME
import app.revanced.patches.youtube.misc.settings.SettingsPatch


object GmsCoreSupportResourcePatch : AbstractGmsCoreSupportResourcePatch(
    fromPackageName = YOUTUBE_PACKAGE_NAME,
    toPackageName = REVANCED_YOUTUBE_PACKAGE_NAME,
    spoofedPackageSignature = "24bb24c05e47e0aefa68a58a766179d9b613a600",
    dependencies = setOf(SettingsPatch::class)
) {
    override fun execute(context: ResourceContext) {
        SettingsPatch.addPreference(
            Preference(
                StringResource("microg_settings", "GmsCore Settings"),
                StringResource("microg_settings_summary", "Settings for GmsCore"),
                Preference.Intent("$gmsCoreVendor.android.gms", "", "org.microg.gms.ui.SettingsActivity")
            )
        )

        val packageName = ChangePackageNamePatch.setOrGetFallbackPackageName(REVANCED_YOUTUBE_PACKAGE_NAME)
        SettingsPatch.renameIntentsTargetPackage(packageName)

        super.execute(context)
    }
}