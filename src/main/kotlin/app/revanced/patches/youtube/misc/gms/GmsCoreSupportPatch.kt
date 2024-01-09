package app.revanced.patches.youtube.misc.gms

import app.revanced.patches.shared.fingerprints.CastContextFetchFingerprint
import app.revanced.patches.shared.misc.gms.AbstractGmsCoreSupportPatch
import app.revanced.patches.youtube.layout.buttons.cast.HideCastButtonPatch
import app.revanced.patches.youtube.misc.fix.playback.ClientSpoofPatch
import app.revanced.patches.youtube.misc.gms.Constants.REVANCED_YOUTUBE_PACKAGE_NAME
import app.revanced.patches.youtube.misc.gms.Constants.YOUTUBE_PACKAGE_NAME
import app.revanced.patches.youtube.misc.gms.GmsCoreSupportResourcePatch.gmsCoreVendorOption
import app.revanced.patches.youtube.misc.gms.fingerprints.*
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.shared.fingerprints.HomeActivityFingerprint


@Suppress("unused")
object GmsCoreSupportPatch : AbstractGmsCoreSupportPatch(
    fromPackageName = YOUTUBE_PACKAGE_NAME,
    toPackageName = REVANCED_YOUTUBE_PACKAGE_NAME,
    primeMethodFingerprint = PrimeMethodFingerprint,
    earlyReturnFingerprints = setOf(
        ServiceCheckFingerprint,
        GooglePlayUtilityFingerprint,
        CastDynamiteModuleFingerprint,
        CastDynamiteModuleV2Fingerprint,
        CastContextFetchFingerprint
    ),
    mainActivityOnCreateFingerprint = HomeActivityFingerprint,
    integrationsPatchDependency = IntegrationsPatch::class,
    dependencies = setOf(
        HideCastButtonPatch::class,
        ClientSpoofPatch::class
    ),
    abstractGmsCoreSupportResourcePatch = GmsCoreSupportResourcePatch,
    compatiblePackages = setOf(
        CompatiblePackage(
            "com.google.android.youtube", setOf(
                "18.32.39",
                "18.37.36",
                "18.38.44",
                "18.43.45",
                "18.44.41",
                "18.45.41",
                "18.45.43"
            )
        )
    ),
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
