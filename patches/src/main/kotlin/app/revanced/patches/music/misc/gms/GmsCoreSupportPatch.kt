package app.revanced.patches.music.misc.gms

import app.revanced.patcher.patch.Option
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.music.misc.fileprovider.fileProviderPatch
import app.revanced.patches.music.misc.gms.Constants.MUSIC_PACKAGE_NAME
import app.revanced.patches.music.misc.gms.Constants.REVANCED_MUSIC_PACKAGE_NAME
import app.revanced.patches.music.misc.settings.PreferenceScreen
import app.revanced.patches.music.misc.settings.settingsPatch
import app.revanced.patches.music.misc.spoof.spoofVideoStreamsPatch
import app.revanced.patches.shared.castContextFetchFingerprint
import app.revanced.patches.shared.misc.gms.gmsCoreSupportPatch
import app.revanced.patches.shared.misc.settings.preference.IntentPreference
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.shared.primeMethodFingerprint

@Suppress("unused")
val gmsCoreSupportPatch = gmsCoreSupportPatch(
    fromPackageName = MUSIC_PACKAGE_NAME,
    toPackageName = REVANCED_MUSIC_PACKAGE_NAME,
    primeMethodFingerprint = primeMethodFingerprint,
    earlyReturnFingerprints = setOf(
        castContextFetchFingerprint,
    ),
    mainActivityOnCreateFingerprint = musicActivityOnCreateFingerprint,
    extensionPatch = sharedExtensionPatch,
    gmsCoreSupportResourcePatchFactory = ::gmsCoreSupportResourcePatch,
) {
    dependsOn(spoofVideoStreamsPatch)

    compatibleWith(
        MUSIC_PACKAGE_NAME(
            "7.29.52",
            "8.10.52",
        ),
    )
}

private fun gmsCoreSupportResourcePatch(
    gmsCoreVendorGroupIdOption: Option<String>,
) = app.revanced.patches.shared.misc.gms.gmsCoreSupportResourcePatch(
    fromPackageName = MUSIC_PACKAGE_NAME,
    toPackageName = REVANCED_MUSIC_PACKAGE_NAME,
    gmsCoreVendorGroupIdOption = gmsCoreVendorGroupIdOption,
    spoofedPackageSignature = "afb0fed5eeaebdd86f56a97742f4b6b33ef59875",
    executeBlock = {
        addResources("shared", "misc.gms.gmsCoreSupportResourcePatch")

        val gmsCoreVendorGroupId by gmsCoreVendorGroupIdOption

        PreferenceScreen.MISC.addPreferences(
            PreferenceScreenPreference(
                "revanced_gms_core_screen",
                preferences = setOf(
                    SwitchPreference("revanced_gms_core_check_updates"),
                    IntentPreference(
                        "revanced_gms_core_settings",
                        intent = IntentPreference.Intent("", "org.microg.gms.ui.SettingsActivity") {
                            "$gmsCoreVendorGroupId.android.gms"
                        },
                    ),
                ),
            ),
        )
    },
) {
    dependsOn(
        addResourcesPatch,
        settingsPatch,
        fileProviderPatch(
            MUSIC_PACKAGE_NAME,
            REVANCED_MUSIC_PACKAGE_NAME,
        ),
    )
}
