package app.revanced.patches.youtube.misc.gms

import app.revanced.patches.shared.fingerprints.CastContextFetchFingerprint
import app.revanced.patches.shared.misc.gms.BaseGmsCoreSupportPatch
import app.revanced.patches.youtube.layout.buttons.cast.HideCastButtonPatch
import app.revanced.patches.youtube.misc.fix.playback.SpoofClientPatch
import app.revanced.patches.youtube.misc.gms.Constants.REVANCED_YOUTUBE_PACKAGE_NAME
import app.revanced.patches.youtube.misc.gms.Constants.YOUTUBE_PACKAGE_NAME
import app.revanced.patches.youtube.misc.gms.GmsCoreSupportResourcePatch.gmsCoreVendorGroupIdOption
import app.revanced.patches.youtube.misc.gms.fingerprints.CastDynamiteModuleV2Fingerprint
import app.revanced.patches.youtube.misc.gms.fingerprints.PrimeMethodFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.shared.fingerprints.MainActivityOnCreateFingerprint

@Suppress("unused")
object GmsCoreSupportPatch : BaseGmsCoreSupportPatch(
    fromPackageName = YOUTUBE_PACKAGE_NAME,
    toPackageName = REVANCED_YOUTUBE_PACKAGE_NAME,
    primeMethodFingerprint = PrimeMethodFingerprint,
    earlyReturnFingerprints = setOf(
        CastDynamiteModuleV2Fingerprint,
        CastContextFetchFingerprint,
    ),
    mainActivityOnCreateFingerprint = MainActivityOnCreateFingerprint,
    integrationsPatchDependency = IntegrationsPatch::class,
    dependencies = setOf(
        HideCastButtonPatch::class,
        SpoofClientPatch::class,
    ),
    gmsCoreSupportResourcePatch = GmsCoreSupportResourcePatch,
    compatiblePackages = setOf(
        CompatiblePackage(
            "com.google.android.youtube",
            setOf(
                "18.37.36",
                "18.38.44",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39",
                "19.03.36",
                "19.04.38",
                "19.05.36",
                "19.06.39",
                "19.07.40",
                "19.08.36",
                "19.09.38",
                "19.10.39",
                "19.11.43",
                "19.12.41",
                "19.13.37",
                "19.14.43",
                "19.15.36",
                "19.16.39",
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
