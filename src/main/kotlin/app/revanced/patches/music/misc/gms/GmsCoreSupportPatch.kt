package app.revanced.patches.music.misc.gms

import app.revanced.patches.music.misc.gms.Constants.MUSIC_PACKAGE_NAME
import app.revanced.patches.music.misc.gms.Constants.REVANCED_MUSIC_PACKAGE_NAME
import app.revanced.patches.music.misc.gms.GmsCoreSupportResourcePatch.gmsCoreVendorGroupIdOption
import app.revanced.patches.music.misc.gms.fingerprints.CastDynamiteModuleV2Fingerprint
import app.revanced.patches.music.misc.gms.fingerprints.MusicActivityOnCreateFingerprint
import app.revanced.patches.music.misc.gms.fingerprints.PrimeMethodFingerprint
import app.revanced.patches.music.misc.integrations.IntegrationsPatch
import app.revanced.patches.shared.fingerprints.CastContextFetchFingerprint
import app.revanced.patches.shared.misc.gms.BaseGmsCoreSupportPatch

@Suppress("unused")
object GmsCoreSupportPatch : BaseGmsCoreSupportPatch(
    fromPackageName = MUSIC_PACKAGE_NAME,
    toPackageName = REVANCED_MUSIC_PACKAGE_NAME,
    primeMethodFingerprint = PrimeMethodFingerprint,
    earlyReturnFingerprints = setOf(
        CastDynamiteModuleV2Fingerprint,
        CastContextFetchFingerprint,
    ),
    mainActivityOnCreateFingerprint = MusicActivityOnCreateFingerprint,
    integrationsPatchDependency = IntegrationsPatch::class,
    gmsCoreSupportResourcePatch = GmsCoreSupportResourcePatch,
    compatiblePackages = setOf(
        CompatiblePackage(
            "com.google.android.apps.youtube.music",
            setOf(
                "6.45.54",
                "6.51.53",
                "7.01.53",
                "7.02.52",
                "7.03.52",
                "7.08.54",
                "7.10.52",
                "7.11.51",
                "7.13.53",
                "7.14.52",
            ),
        ),
    ),
    fingerprints = setOf(
        CastDynamiteModuleV2Fingerprint,
        CastContextFetchFingerprint,
        PrimeMethodFingerprint,
    ),
) {
    override val gmsCoreVendorGroupId by gmsCoreVendorGroupIdOption
}
