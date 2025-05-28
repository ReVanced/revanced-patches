package app.revanced.patches.youtube.layout.shortsplayer

import app.revanced.patcher.fingerprint
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags

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

// Pre 19.25
internal val shortsPlaybackIntentLegacyFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters(
        "L",
        "Ljava/util/Map;",
        "J",
        "Ljava/lang/String;",
        "Z",
        "Ljava/util/Map;"
    )
    strings(
        // None of these strings are unique.
        "com.google.android.apps.youtube.app.endpoint.flags",
        "ReelWatchFragmentArgs",
        "reels_fragment_descriptor"
    )
}

internal val shortsPlaybackIntentFingerprint = fingerprint {
    accessFlags(AccessFlags.PROTECTED, AccessFlags.FINAL)
    returns("V")
    parameters(
        "Lcom/google/android/libraries/youtube/player/model/PlaybackStartDescriptor;",
        "Ljava/util/Map;",
        "J",
        "Ljava/lang/String;"
    )
    strings(
        // None of these strings are unique.
        "com.google.android.apps.youtube.app.endpoint.flags",
        "ReelWatchFragmentArgs",
        "reels_fragment_descriptor"
    )
}

internal val exitVideoPlayerFingerprint = fingerprint {
    returns("V")
    parameters()
    literal {
        mdx_drawer_layout_id
    }
}