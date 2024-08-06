package app.revanced.patches.googlenews.misc.gms

import app.revanced.patches.googlenews.misc.gms.Constants.MAGAZINES_PACKAGE_NAME
import app.revanced.patches.googlenews.misc.gms.Constants.REVANCED_MAGAZINES_PACKAGE_NAME
import app.revanced.patches.googlenews.misc.gms.GmsCoreSupportResourcePatch.gmsCoreVendorGroupIdOption
import app.revanced.patches.googlenews.misc.gms.fingerprints.MagazinesActivityOnCreateFingerprint
import app.revanced.patches.googlenews.misc.integrations.IntegrationsPatch
import app.revanced.patches.shared.misc.gms.BaseGmsCoreSupportPatch

@Suppress("unused")
object GmsCoreSupportPatch : BaseGmsCoreSupportPatch(
    fromPackageName = MAGAZINES_PACKAGE_NAME,
    toPackageName = REVANCED_MAGAZINES_PACKAGE_NAME,
    primeMethodFingerprint = null,
    mainActivityOnCreateFingerprint = MagazinesActivityOnCreateFingerprint,
    integrationsPatchDependency = IntegrationsPatch::class,
    gmsCoreSupportResourcePatch = GmsCoreSupportResourcePatch,
    // Remove version constraint,
    // once https://github.com/ReVanced/revanced-patches/pull/3111#issuecomment-2240877277 is resolved.
    compatiblePackages = setOf(CompatiblePackage(MAGAZINES_PACKAGE_NAME, setOf("5.108.0.644447823"))),
) {
    override val gmsCoreVendorGroupId by gmsCoreVendorGroupIdOption
}
