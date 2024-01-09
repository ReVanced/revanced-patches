package app.revanced.patches.music.misc.gms

import app.revanced.patches.music.misc.gms.Constants.MUSIC_PACKAGE_NAME
import app.revanced.patches.music.misc.gms.Constants.REVANCED_MUSIC_PACKAGE_NAME
import app.revanced.patches.music.misc.gms.GmsCoreSupportResourcePatch.gmsCoreVendorOption
import app.revanced.patches.music.misc.gms.fingerprints.*
import app.revanced.patches.shared.misc.gms.AbstractGmsCoreSupportPatch
import app.revanced.patches.youtube.misc.gms.fingerprints.CastContextFetchFingerprint

@Suppress("unused")
object GmsCoreSupportPatch : AbstractGmsCoreSupportPatch(
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
    abstractGmsCoreSupportResourcePatch = GmsCoreSupportResourcePatch,
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
