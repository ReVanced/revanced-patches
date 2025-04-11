package app.revanced.patches.googlephotos.misc.gms

import app.revanced.patcher.patch.Option
import app.revanced.patches.googlephotos.misc.extension.extensionPatch
import app.revanced.patches.googlephotos.misc.gms.Constants.PHOTOS_PACKAGE_NAME
import app.revanced.patches.googlephotos.misc.gms.Constants.REVANCED_PHOTOS_PACKAGE_NAME
import app.revanced.patches.shared.misc.gms.gmsCoreSupportPatch
import app.revanced.util.isAndroidRuntime
import java.util.logging.Logger

@Suppress("unused")
val gmsCoreSupportPatch = gmsCoreSupportPatch(
    fromPackageName = PHOTOS_PACKAGE_NAME,
    toPackageName = REVANCED_PHOTOS_PACKAGE_NAME,
    mainActivityOnCreateFingerprint = homeActivityOnCreateFingerprint,
    extensionPatch = extensionPatch,
    gmsCoreSupportResourcePatchFactory = ::gmsCoreSupportResourcePatch,
    executeBlock = {
        // Resource compilation fails if patching inside ReVanced Manager.
        // This check is only needed for GmsCore support since all other patches
        // are bytecode and should work if using root install without GmsCore.
        // TODO: Remove this logic after Manager resource compilation is fixed.
        if (isAndroidRuntime()) {
            Logger.getLogger(this::class.java.name).severe(
                """
                    !!! Patching in this environment is not yet supported.
                    !!!
                    !!! Google Photos cannot be patched using ReVanced Manager.
                    !!!
                    !!! To Patch Google Photos, please patch using command prompt and ReVanced CLI tools.
                    !!! 
                """.trimIndent()
            )

            // Add a delay, so the user is more likely to read the warning.
            Thread.sleep(30_000)
        }
    }
) {
    compatibleWith(PHOTOS_PACKAGE_NAME)
}

private fun gmsCoreSupportResourcePatch(
    gmsCoreVendorGroupIdOption: Option<String>,
) = app.revanced.patches.shared.misc.gms.gmsCoreSupportResourcePatch(
    fromPackageName = PHOTOS_PACKAGE_NAME,
    toPackageName = REVANCED_PHOTOS_PACKAGE_NAME,
    spoofedPackageSignature = "24bb24c05e47e0aefa68a58a766179d9b613a600",
    gmsCoreVendorGroupIdOption = gmsCoreVendorGroupIdOption,
)
