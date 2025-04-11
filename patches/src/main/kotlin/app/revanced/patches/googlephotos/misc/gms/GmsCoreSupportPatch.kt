package app.revanced.patches.googlephotos.misc.gms

import app.revanced.patcher.patch.Option
import app.revanced.patcher.patch.PatchException
import app.revanced.patches.googlephotos.misc.extension.extensionPatch
import app.revanced.patches.googlephotos.misc.gms.Constants.PHOTOS_PACKAGE_NAME
import app.revanced.patches.googlephotos.misc.gms.Constants.REVANCED_PHOTOS_PACKAGE_NAME
import app.revanced.patches.shared.misc.gms.gmsCoreSupportPatch
import app.revanced.util.isAndroidRuntime

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
        if (isAndroidRuntime) {
            throw PatchException(
                """
                    !!!
                    !!!
                    !!! At this time, Google Photos cannot 
                    !!! be patched using ReVanced Manager.
                    !!!
                    !!! Until this limitation is fixed,
                    !!! you can patch Google Photos
                    !!! use ReVanced CLI:
                    !!!
                    !!! https://github.com/revanced/revanced-cli
                    !!! 
                """.trimIndent()
            )
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
