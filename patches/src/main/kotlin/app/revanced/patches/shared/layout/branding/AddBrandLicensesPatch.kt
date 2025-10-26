package app.revanced.patches.shared.layout.branding

import app.revanced.patcher.patch.resourcePatch
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources

/**
 * Copies a branding license text file to the target apk.
 *
 * This patch must be a dependency for all patches that add ReVanced branding to the target app.
 */
internal val addBrandLicensesPatch = resourcePatch {
    execute {
        copyResources(
            "branding-license",
            ResourceGroup(
                "raw",
                "revanced_license_branding.txt"
            )
        )
    }
}
