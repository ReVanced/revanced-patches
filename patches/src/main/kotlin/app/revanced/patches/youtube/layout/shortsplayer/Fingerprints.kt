package app.revanced.patches.youtube.layout.shortsplayer

import app.revanced.patcher.fingerprint
import app.revanced.patcher.literal
import app.revanced.patcher.methodCall
import app.revanced.patcher.string
import app.revanced.patches.shared.misc.mapping.resourceLiteral
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Purpose of this method is not clear, and it's only used to identify
 * the obfuscated name of the videoId() method in PlaybackStartDescriptor.
 */
internal val playbackStartFeatureFlagFingerprint by fingerprint {
    returns("Z")
    parameters(
        "Lcom/google/android/libraries/youtube/player/model/PlaybackStartDescriptor;",
    )
    instructions(
        methodCall(
            definingClass = "Lcom/google/android/libraries/youtube/player/model/PlaybackStartDescriptor;",
            returnType = "Ljava/lang/String;"
        ),
        literal(45380134L)
    )
}

// Pre 19.25
internal val shortsPlaybackIntentLegacyFingerprint by fingerprint {
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
    instructions(
        methodCall(returnType = "Lcom/google/android/libraries/youtube/player/model/PlaybackStartDescriptor;"),
        // None of these strings are unique.
        string("com.google.android.apps.youtube.app.endpoint.flags"),
        string("ReelWatchFragmentArgs"),
        string("reels_fragment_descriptor")
    )
}

internal val shortsPlaybackIntentFingerprint by fingerprint {
    accessFlags(AccessFlags.PROTECTED, AccessFlags.FINAL)
    returns("V")
    parameters(
        "Lcom/google/android/libraries/youtube/player/model/PlaybackStartDescriptor;",
        "Ljava/util/Map;",
        "J",
        "Ljava/lang/String;"
    )
    instructions(
        // None of these strings are unique.
        string("com.google.android.apps.youtube.app.endpoint.flags"),
        string("ReelWatchFragmentArgs"),
        string("reels_fragment_descriptor")
    )
}

internal val exitVideoPlayerFingerprint by fingerprint {
    returns("V")
    parameters()
    instructions(
        resourceLiteral("id", "mdx_drawer_layout")
    )
}