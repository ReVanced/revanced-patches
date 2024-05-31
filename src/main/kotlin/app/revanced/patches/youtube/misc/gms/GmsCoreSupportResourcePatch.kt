package app.revanced.patches.youtube.misc.gms

import app.revanced.patcher.patch.Option
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.gms.gmsCoreSupportResourcePatch
import app.revanced.patches.shared.misc.settings.preference.IntentPreference
import app.revanced.patches.youtube.misc.gms.Constants.REVANCED_YOUTUBE_PACKAGE_NAME
import app.revanced.patches.youtube.misc.gms.Constants.YOUTUBE_PACKAGE_NAME
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch

internal fun gmsCoreSupportResourcePatch(
    gmsCoreVendorGroupIdOption: Option<String>,
) = gmsCoreSupportResourcePatch(
    fromPackageName = YOUTUBE_PACKAGE_NAME,
    toPackageName = REVANCED_YOUTUBE_PACKAGE_NAME,
    gmsCoreVendorGroupIdOption = gmsCoreVendorGroupIdOption,
    spoofedPackageSignature = "24bb24c05e47e0aefa68a58a766179d9b613a600",
    executeBlock = {
        addResources("youtube", "misc.gms.GmsCoreSupportResourcePatch")

        val gmsCoreVendorGroupId by gmsCoreVendorGroupIdOption

        PreferenceScreen.MISC.addPreferences(
            IntentPreference(
                "microg_settings",
                intent = IntentPreference.Intent("", "org.microg.gms.ui.SettingsActivity") {
                    "$gmsCoreVendorGroupId.android.gms"
                },
            ),
        )
    },
) {
    dependsOn(settingsPatch, addResourcesPatch)
}
