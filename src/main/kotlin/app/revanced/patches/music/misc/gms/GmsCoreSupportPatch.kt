package app.revanced.patches.music.misc.gms

import app.revanced.patches.music.misc.gms.Constants.MUSIC_PACKAGE_NAME
import app.revanced.patches.music.misc.gms.Constants.REVANCED_MUSIC_PACKAGE_NAME
import app.revanced.patches.music.misc.gms.GmsCoreSupportResourcePatch.gmsCoreVendorOption
import app.revanced.patches.music.misc.gms.fingerprints.*
import app.revanced.patches.music.misc.integrations.fingerprints.ApplicationInitFingerprint
import app.revanced.patches.music.misc.integrations.IntegrationsPatch
import app.revanced.patches.shared.misc.gms.BaseGmsCoreSupportPatch
import app.revanced.patches.shared.fingerprints.CastContextFetchFingerprint

@Suppress("unused")
object GmsCoreSupportPatch : BaseGmsCoreSupportPatch(
    fromPackageName = MUSIC_PACKAGE_NAME,
    toPackageName = REVANCED_MUSIC_PACKAGE_NAME,
    primeMethodFingerprint = PrimeMethodFingerprint,
    earlyReturnFingerprints = setOf(
        ServiceCheckFingerprint,
        GooglePlayUtilityFingerprint,
        CastDynamiteModuleFingerprint,
        CastDynamiteModuleV2Fingerprint,
        CastContextFetchFingerprint,
    ),
    mainActivityOnCreateFingerprint = ApplicationInitFingerprint,
    integrationsPatchDependency = IntegrationsPatch::class,
    gmsCoreSupportResourcePatch = GmsCoreSupportResourcePatch,
    compatiblePackages = setOf(CompatiblePackage("com.google.android.apps.youtube.music")),
    fingerprints = setOf(
        ServiceCheckFingerprint,
        GooglePlayUtilityFingerprint,
        CastDynamiteModuleFingerprint,
        CastDynamiteModuleV2Fingerprint,
        CastContextFetchFingerprint,
        PrimeMethodFingerprint,
    )
) {
    override val gmsCoreVendor by gmsCoreVendorOption
}
