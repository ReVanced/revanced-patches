package app.revanced.patches.music.misc.dns

import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.music.shared.mainActivityOnCreateFingerprint
import app.revanced.patches.shared.misc.dns.checkWatchHistoryDomainNameResolutionPatch

val checkWatchHistoryDomainNameResolutionPatch = checkWatchHistoryDomainNameResolutionPatch(
    block = {
        dependsOn(
            sharedExtensionPatch
        )

        compatibleWith(
            "com.google.android.apps.youtube.music"(
                "7.29.52",
                "8.10.52"
            )
        )
    },

    mainActivityFingerprint = mainActivityOnCreateFingerprint
)
