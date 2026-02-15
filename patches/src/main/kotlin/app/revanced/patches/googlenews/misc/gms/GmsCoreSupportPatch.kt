package app.revanced.patches.googlenews.misc.gms

import app.revanced.patcher.patch.Option
import app.revanced.patches.googlenews.misc.extension.extensionPatch
import app.revanced.patches.googlenews.misc.gms.Constants.MAGAZINES_PACKAGE_NAME
import app.revanced.patches.googlenews.misc.gms.Constants.REVANCED_MAGAZINES_PACKAGE_NAME
import app.revanced.patches.shared.misc.gms.gmsCoreSupportPatch
import app.revanced.patches.shared.misc.gms.gmsCoreSupportResourcePatch

@Suppress("unused")
val gmsCoreSupportPatch = gmsCoreSupportPatch(
    fromPackageName = MAGAZINES_PACKAGE_NAME,
    toPackageName = REVANCED_MAGAZINES_PACKAGE_NAME,
    mainActivityOnCreateFingerprint = magazinesActivityOnCreateFingerprint,
    extensionPatch = extensionPatch,
    gmsCoreSupportResourcePatchFactory = ::gmsCoreSupportResourcePatch,
) {
    // Remove version constraint,
    // once https://github.com/ReVanced/revanced-patches/pull/3111#issuecomment-2240877277 is resolved.
    compatibleWith(MAGAZINES_PACKAGE_NAME("5.108.0.644447823"))
}

private fun gmsCoreSupportResourcePatch(
    gmsCoreVendorGroupIdOption: Option<String>,
) = gmsCoreSupportResourcePatch(
    fromPackageName = MAGAZINES_PACKAGE_NAME,
    toPackageName = REVANCED_MAGAZINES_PACKAGE_NAME,
    spoofedPackageSignature = "24bb24c05e47e0aefa68a58a766179d9b613a666",
    gmsCoreVendorGroupIdOption = gmsCoreVendorGroupIdOption,
)
