package app.revanced.patches.music.misc.spoof

import app.revanced.patches.shared.misc.spoof.spoofVideoStreamsPatch

val spoofVideoStreamsPatch = spoofVideoStreamsPatch(
    block = {
        compatibleWith(
            "com.google.android.apps.youtube.music"(
                "7.29.52"
            )
        )

        dependsOn(userAgentClientSpoofPatch)
    },
    fixMediaFetchHotConfigChanges = { true }
)