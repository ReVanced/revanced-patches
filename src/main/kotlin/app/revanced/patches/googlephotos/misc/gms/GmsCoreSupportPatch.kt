package app.revanced.patches.googlephotos.misc.gms

import app.revanced.patches.googlephotos.misc.gms.Constants.PHOTOS_PACKAGE_NAME
import app.revanced.patches.googlephotos.misc.gms.Constants.REVANCED_PHOTOS_PACKAGE_NAME
import app.revanced.patches.googlephotos.misc.gms.GmsCoreSupportResourcePatch.gmsCoreVendorGroupIdOption
import app.revanced.patches.googlephotos.misc.gms.fingerprints.PhotosActivityOnCreateFingerprint
import app.revanced.patches.googlephotos.misc.gms.fingerprints.ServiceCheckFingerprint
import app.revanced.patches.googlephotos.misc.gms.fingerprints.CastDynamiteModuleFingerprint
import app.revanced.patches.googlephotos.misc.integrations.IntegrationsPatch
import app.revanced.patches.shared.misc.gms.BaseGmsCoreSupportPatch

@Suppress("unused")
object GmsCoreSupportPatch : BaseGmsCoreSupportPatch(
    fromPackageName = PHOTOS_PACKAGE_NAME,
    toPackageName = REVANCED_PHOTOS_PACKAGE_NAME,
    primeMethodFingerprint = null,
    earlyReturnFingerprints = setOf(
    ServiceCheckFingerprint,
    CastDynamiteModuleFingerprint),
    mainActivityOnCreateFingerprint = PhotosActivityOnCreateFingerprint,
    integrationsPatchDependency = IntegrationsPatch::class,
    gmsCoreSupportResourcePatch = GmsCoreSupportResourcePatch,
    compatiblePackages = setOf(CompatiblePackage(PHOTOS_PACKAGE_NAME)),
    fingerprints = setOf(
        ServiceCheckFingerprint,
        CastDynamiteModuleFingerprint),
) {
    override val gmsCoreVendorGroupId by gmsCoreVendorGroupIdOption
}
