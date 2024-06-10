package app.revanced.patches.youtube.misc.gms

import app.revanced.patches.shared.fingerprints.*
import app.revanced.patches.shared.fingerprints.castDynamiteModuleFingerprint
import app.revanced.patches.shared.fingerprints.castDynamiteModuleV2Fingerprint
import app.revanced.patches.shared.fingerprints.primeMethodFingerprint
import app.revanced.patches.shared.misc.gms.gmsCoreSupportPatch
import app.revanced.patches.youtube.layout.buttons.cast.hideCastButtonPatch
import app.revanced.patches.youtube.misc.fix.playback.spoofClientPatch
import app.revanced.patches.youtube.misc.gms.Constants.REVANCED_YOUTUBE_PACKAGE_NAME
import app.revanced.patches.youtube.misc.gms.Constants.YOUTUBE_PACKAGE_NAME
import app.revanced.patches.youtube.misc.gms.fingerprints.*
import app.revanced.patches.youtube.misc.integrations.integrationsPatch
import app.revanced.patches.youtube.shared.fingerprints.mainActivityOnCreateFingerprint

@Suppress("unused")
val gmsCoreSupportPatch = gmsCoreSupportPatch(
    fromPackageName = YOUTUBE_PACKAGE_NAME,
    toPackageName = REVANCED_YOUTUBE_PACKAGE_NAME,
    primeMethodFingerprint = primeMethodFingerprint,
    earlyReturnFingerprints = setOf(
        serviceCheckFingerprint,
        googlePlayUtilityFingerprint,
        castDynamiteModuleFingerprint,
        castDynamiteModuleV2Fingerprint,
        castContextFetchFingerprint,
    ),
    mainActivityOnCreateFingerprint = mainActivityOnCreateFingerprint,
    integrationsPatch = integrationsPatch,
    gmsCoreSupportResourcePatchFactory = ::gmsCoreSupportResourcePatch,
) {
    dependsOn(
        hideCastButtonPatch,
        spoofClientPatch,
    )

    compatibleWith(
        YOUTUBE_PACKAGE_NAME(
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
    )
}
