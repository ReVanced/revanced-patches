package app.revanced.patches.magazines.misc.gms

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patches.shared.misc.gms.BaseGmsCoreSupportPatch
import app.revanced.patches.magazines.misc.gms.Constants.MAGAZINES_PACKAGE_NAME
import app.revanced.patches.magazines.misc.gms.Constants.REVANCED_MAGAZINES_PACKAGE_NAME
import app.revanced.patches.magazines.misc.gms.GmsCoreSupportResourcePatch.gmsCoreVendorGroupIdOption
import app.revanced.patches.magazines.misc.integrations.IntegrationsPatch
import app.revanced.patches.magazines.misc.gms.fingerprints.MagazinesActivityOnCreateFingerprint
import app.revanced.patches.magazines.misc.gms.fingerprints.GooglePlayUtilityFingerprint
import app.revanced.patches.magazines.misc.gms.fingerprints.ServiceCheckFingerprint

@Suppress("unused")
object GmsCoreSupportPatch : BaseGmsCoreSupportPatch(
    fromPackageName = MAGAZINES_PACKAGE_NAME,
    toPackageName = REVANCED_MAGAZINES_PACKAGE_NAME,
    primeMethodFingerprint = null,
    earlyReturnFingerprints = setOf(
        ServiceCheckFingerprint,
        GooglePlayUtilityFingerprint,
    ),
    mainActivityOnCreateFingerprint = MagazinesActivityOnCreateFingerprint,
    integrationsPatchDependency = IntegrationsPatch::class,
    dependencies = setOf(
    ),
    gmsCoreSupportResourcePatch = GmsCoreSupportResourcePatch,
    compatiblePackages = setOf(
        CompatiblePackage(
            MAGAZINES_PACKAGE_NAME,
        ),
    ),
    fingerprints = setOf(
        ServiceCheckFingerprint,
        GooglePlayUtilityFingerprint,
    ),
) {
    override val gmsCoreVendorGroupId by gmsCoreVendorGroupIdOption

    override fun execute(context: BytecodeContext) {
        super.execute(context)
    }
}
