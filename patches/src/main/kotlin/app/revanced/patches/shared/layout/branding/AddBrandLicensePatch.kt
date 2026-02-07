package app.revanced.patches.shared.layout.branding

import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.rawResourcePatch
import app.revanced.util.inputStreamFromBundledResource
import java.nio.file.Files
import java.util.logging.Logger

/**
 * Copies a branding license text file to the target apk.
 *
 * This patch must be a dependency for all patches that add ReVanced branding to the target app.
 */
internal val addBrandLicensePatch = rawResourcePatch {
    apply {
        val brandingLicenseFileName = "LICENSE_REVANCED.TXT"

        val inputFileStream = inputStreamFromBundledResource(
            "branding-license",
            brandingLicenseFileName
        )!!

        val targetFile = get(brandingLicenseFileName, false).toPath()

        if (Files.exists(targetFile)) Logger.getLogger(this::class.java.name)
            .warning("Already patched by ReVanced")
        else Files.copy(inputFileStream, targetFile)
    }
}
