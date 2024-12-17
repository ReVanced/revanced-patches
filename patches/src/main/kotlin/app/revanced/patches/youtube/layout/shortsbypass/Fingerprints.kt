package app.revanced.patches.youtube.layout.shortsbypass

import app.revanced.patcher.fingerprint
import app.revanced.util.literal

/**
 * Purpose of this method is not clear, and it's only used to identify
 * the obfuscated name of the videoId() method in PlaybackStartDescriptor.
 */
internal val playbackStartFeatureFlagFingerprint = fingerprint {
    returns("Z")
    parameters(
        "Lcom/google/android/libraries/youtube/player/model/PlaybackStartDescriptor;",
    )
    literal {
        45380134L
    }
}

internal val playbackStartDescriptorFingerprint = fingerprint {
    returns("V")
    parameters(
        "Lcom/google/android/libraries/youtube/player/model/PlaybackStartDescriptor;",
        "Ljava/util/Map;",
        "J",
        "Ljava/lang/String;"
    )
    strings(
        // None of these strings are unique.
        "com.google.android.apps.youtube.PlaybackStartDescriptor",
        "PLAYBACK_START_DESCRIPTOR_MUTATOR",
        "ReelWatchFragmentArgs"
    )
}


