package app.revanced.patches.youtube.misc.microg.patch.resource

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.shared.settings.preference.impl.Preference
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.youtube.misc.microg.shared.Constants.PACKAGE_NAME
import app.revanced.patches.youtube.misc.microg.shared.Constants.REVANCED_APP_NAME
import app.revanced.patches.youtube.misc.microg.shared.Constants.REVANCED_PACKAGE_NAME
import app.revanced.patches.youtube.misc.microg.shared.Constants.SPOOFED_PACKAGE_NAME
import app.revanced.patches.youtube.misc.microg.shared.Constants.SPOOFED_PACKAGE_SIGNATURE
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.settings.resource.patch.SettingsResourcePatch
import app.revanced.util.microg.Constants.MICROG_VENDOR
import app.revanced.util.microg.MicroGManifestHelper
import app.revanced.util.microg.MicroGResourceHelper

@DependsOn([SettingsResourcePatch::class])
class MicroGResourcePatch : ResourcePatch {
    override fun execute(context: ResourceContext) {
        SettingsPatch.addPreference(
            Preference(
                StringResource("microg_settings", "MicroG Settings"),
                StringResource("microg_settings_summary", "Settings for MicroG"),
                Preference.Intent("$MICROG_VENDOR.android.gms", "", "org.microg.gms.ui.SettingsActivity")
            )
        )
        SettingsPatch.renameIntentsTargetPackage(REVANCED_PACKAGE_NAME)

        // update manifest
        MicroGResourceHelper.patchManifest(
            context,
            PACKAGE_NAME,
            REVANCED_PACKAGE_NAME,
            REVANCED_APP_NAME
        )

        // add metadata to manifest
        MicroGManifestHelper.addSpoofingMetadata(
            context,
            SPOOFED_PACKAGE_NAME,
            SPOOFED_PACKAGE_SIGNATURE
        )

        // add strings
        MicroGResourceHelper.addStrings(context)
    }
}