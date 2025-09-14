package app.revanced.patches.music.misc.spoof

import app.revanced.patches.music.playservice.is_7_33_or_greater
import app.revanced.patches.music.playservice.is_8_11_or_greater
import app.revanced.patches.music.playservice.versionCheckPatch
import app.revanced.patches.shared.misc.spoof.spoofVideoStreamsPatch

val spoofVideoStreamsPatch = spoofVideoStreamsPatch(
    block = {
        compatibleWith(
            "com.google.android.apps.youtube.music"(
                "7.29.52"
            )
        )

        dependsOn(versionCheckPatch, userAgentClientSpoofPatch)
    },
    fixMediaFetchHotConfigChanges = { true },
    fixMediaFetchHotConfigAlternativeChanges = { is_8_11_or_greater },
    fixParsePlaybackResponseFeatureFlag = { is_7_33_or_greater }
)