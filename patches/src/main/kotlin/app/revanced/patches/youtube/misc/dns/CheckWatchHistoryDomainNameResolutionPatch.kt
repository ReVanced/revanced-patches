package app.revanced.patches.youtube.misc.dns

import app.revanced.patches.shared.misc.dns.checkWatchHistoryDomainNameResolutionPatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.shared.mainActivityOnCreateFingerprint

val checkWatchHistoryDomainNameResolutionPatch = checkWatchHistoryDomainNameResolutionPatch(
    block = {
        dependsOn(
            sharedExtensionPatch
        )

        compatibleWith(
            "com.google.android.youtube"(
                "19.43.41",
                "20.14.43",
                "20.21.37",
                "20.31.40",
            )
        )
    },
    mainActivityFingerprint = mainActivityOnCreateFingerprint
)
