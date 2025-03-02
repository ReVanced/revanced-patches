package app.revanced.patches.googlephotos.misc.gms

import app.revanced.patcher.patch.Option
import app.revanced.patches.googlephotos.misc.extension.extensionPatch
import app.revanced.patches.googlephotos.misc.gms.Constants.PHOTOS_PACKAGE_NAME
import app.revanced.patches.googlephotos.misc.gms.Constants.REVANCED_PHOTOS_PACKAGE_NAME
import app.revanced.patches.shared.misc.gms.gmsCoreSupportPatch

@Suppress("unused")
val gmsCoreSupportPatch = gmsCoreSupportPatch(
    fromPackageName = PHOTOS_PACKAGE_NAME,
    toPackageName = REVANCED_PHOTOS_PACKAGE_NAME,
    mainActivityOnCreateFingerprint = homeActivityOnCreateFingerprint,
    extensionPatch = extensionPatch,
    gmsCoreSupportResourcePatchFactory = ::gmsCoreSupportResourcePatch,
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
