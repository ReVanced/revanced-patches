package app.revanced.patches.youtube.misc.gms

import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.Option
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.castContextFetchMethod
import app.revanced.patches.shared.misc.gms.gmsCoreSupportPatch
import app.revanced.patches.shared.misc.settings.preference.IntentPreference
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.shared.primeMethod
import app.revanced.patches.youtube.layout.buttons.overlay.hidePlayerOverlayButtonsPatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.gms.Constants.REVANCED_YOUTUBE_PACKAGE_NAME
import app.revanced.patches.youtube.misc.gms.Constants.YOUTUBE_PACKAGE_NAME
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.misc.spoof.spoofVideoStreamsPatch
import app.revanced.patches.youtube.shared.mainActivityOnCreateMethod

@Suppress("unused")
val gmsCoreSupportPatch = gmsCoreSupportPatch(
    fromPackageName = YOUTUBE_PACKAGE_NAME,
    toPackageName = REVANCED_YOUTUBE_PACKAGE_NAME,
    getPrimeMethod = BytecodePatchContext::primeMethod::get,
    getEarlyReturnMethods = setOf(BytecodePatchContext::castContextFetchMethod::get),
    getMainActivityOnCreateMethodToGetInsertIndex = BytecodePatchContext::mainActivityOnCreateMethod::get to { 0 },
    extensionPatch = sharedExtensionPatch,
    gmsCoreSupportResourcePatchFactory = ::gmsCoreSupportResourcePatch,
) {
    dependsOn(
        hidePlayerOverlayButtonsPatch, // Hide non-functional cast button.
        spoofVideoStreamsPatch,
    )

    compatibleWith(
        YOUTUBE_PACKAGE_NAME(
            "19.43.41",
            "20.14.43",
            "20.21.37",
            "20.31.40",
        ),
    )
}

private fun gmsCoreSupportResourcePatch(
    gmsCoreVendorGroupIdOption: Option<String>,
) = app.revanced.patches.shared.misc.gms.gmsCoreSupportResourcePatch(
    fromPackageName = YOUTUBE_PACKAGE_NAME,
    toPackageName = REVANCED_YOUTUBE_PACKAGE_NAME,
    gmsCoreVendorGroupIdOption = gmsCoreVendorGroupIdOption,
    spoofedPackageSignature = "24bb24c05e47e0aefa68a58a766179d9b613a600",
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
        accountCredentialsInvalidTextPatch,
    )
}
