package app.revanced.patches.youtube.utils.microg

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.patch.microg.MicroGManifestHelper.addSpoofingMetadata
import app.revanced.patches.shared.patch.microg.MicroGResourceHelper.patchManifest
import app.revanced.patches.shared.patch.microg.MicroGResourceHelper.patchSetting
import app.revanced.patches.shared.patch.packagename.PackageNamePatch
import app.revanced.patches.youtube.utils.microg.Constants.PACKAGE_NAME
import app.revanced.patches.youtube.utils.microg.Constants.SPOOFED_PACKAGE_NAME
import app.revanced.patches.youtube.utils.microg.Constants.SPOOFED_PACKAGE_SIGNATURE
import app.revanced.patches.youtube.utils.settings.ResourceUtils.setMicroG
import app.revanced.patches.youtube.utils.settings.SettingsPatch

@Patch(
    name = "MicroG support",
    description = "Allows ReVanced Extended to run without root and under a different package name with MicroG.",
    dependencies = [
        MicroGBytecodePatch::class,
        PackageNamePatch::class,
        SettingsPatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.25.40",
                "18.27.36",
                "18.29.38",
                "18.30.37",
                "18.31.40",
                "18.32.39",
                "18.33.40",
                "18.34.38",
                "18.35.36",
                "18.36.39",
                "18.37.36",
                "18.38.44",
                "18.39.41",
                "18.40.34",
                "18.41.39",
                "18.42.41",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.46.45",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39"
            ]
        )
    ]
)
@Suppress("unused")
object MicroGPatch : ResourcePatch() {
    override fun execute(context: ResourceContext) {

        val packageName = PackageNamePatch.PackageNameYouTube
            ?: throw PatchException("Invalid package name.")

        if (packageName == PACKAGE_NAME)
            throw PatchException("Original package name is not available as package name for MicroG build.")

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: MICROG_SETTINGS"
            )
        )
        SettingsPatch.updatePatchStatus("MicroG support")

        // update settings fragment
        context.patchSetting(
            PACKAGE_NAME,
            packageName
        )

        // update manifest
        context.patchManifest(
            PACKAGE_NAME,
            packageName
        )

        // add metadata to manifest
        context.addSpoofingMetadata(
            SPOOFED_PACKAGE_NAME,
            SPOOFED_PACKAGE_SIGNATURE
        )

        setMicroG(packageName)

    }
}