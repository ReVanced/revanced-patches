package app.revanced.patches.youtube.misc.microg.patch.resource

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.youtube.misc.manifest.patch.FixLocaleConfigErrorPatch
import app.revanced.patches.youtube.misc.microg.annotations.MicroGPatchCompatibility
import app.revanced.patches.youtube.misc.microg.shared.Constants.PACKAGE_NAME
import app.revanced.patches.youtube.misc.microg.shared.Constants.REVANCED_APP_NAME
import app.revanced.patches.youtube.misc.microg.shared.Constants.REVANCED_PACKAGE_NAME
import app.revanced.patches.youtube.misc.microg.shared.Constants.SPOOFED_PACKAGE_NAME
import app.revanced.patches.youtube.misc.microg.shared.Constants.SPOOFED_PACKAGE_SIGNATURE
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.settings.framework.components.impl.Preference
import app.revanced.patches.youtube.misc.settings.framework.components.impl.StringResource
import app.revanced.patches.youtube.misc.settings.resource.patch.SettingsResourcePatch
import app.revanced.util.microg.Constants.MICROG_VENDOR
import app.revanced.util.microg.MicroGManifestHelper
import app.revanced.util.microg.MicroGResourceHelper

@Name("microg-resource-patch")
@DependsOn([FixLocaleConfigErrorPatch::class, SettingsResourcePatch::class])
@Description("Resource patch to allow YouTube ReVanced to run without root and under a different package name.")
@MicroGPatchCompatibility
@Version("0.0.1")
class MicroGResourcePatch : ResourcePatch {
    override fun execute(context: ResourceContext): PatchResult {
        SettingsPatch.addPreference(
            Preference(
                StringResource("microg_settings", "MicroG Settings"),
                Preference.Intent("$MICROG_VENDOR.android.gms", "", "org.microg.gms.ui.SettingsActivity"),
                StringResource("microg_settings_summary", "Settings for MicroG"),
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
        return PatchResultSuccess()
    }
}