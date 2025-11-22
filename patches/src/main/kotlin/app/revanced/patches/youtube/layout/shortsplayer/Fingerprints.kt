package app.revanced.patches.youtube.layout.shortsplayer

import app.revanced.patcher.checkCast
import app.revanced.patcher.fieldAccess
import app.revanced.patcher.fingerprint
import app.revanced.patcher.literal
import app.revanced.patcher.methodCall
import app.revanced.patcher.string
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.shared.misc.mapping.resourceLiteral
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * Purpose of this method is not clear, and it's only used to identify
 * the obfuscated name of the videoId() method in PlaybackStartDescriptor.
 * 20.38 and lower.
 */
internal val playbackStartFeatureFlagFingerprint = fingerprint {
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

/**
 * Purpose of this method is not entirely clear, and it's only used to identify
 * the obfuscated name of the videoId() method in PlaybackStartDescriptor.
 * 20.39+
 */
internal val watchPanelVideoIdFingerprint = fingerprint {
    returns("Ljava/lang/String;")
    parameters()
    instructions(
        fieldAccess(
            opcode = Opcode.IGET_OBJECT,
            type = "Lcom/google/android/apps/youtube/app/common/player/queue/WatchPanelId;"
        ),
        checkCast("Lcom/google/android/apps/youtube/app/common/player/queue/DefaultWatchPanelId;"),
        methodCall(
            definingClass = "Lcom/google/android/apps/youtube/app/common/player/queue/DefaultWatchPanelId;",
            returnType = "Lcom/google/android/libraries/youtube/player/model/PlaybackStartDescriptor;"
        ),
        methodCall(
            definingClass = "Lcom/google/android/libraries/youtube/player/model/PlaybackStartDescriptor;",
            returnType = "Ljava/lang/String;"
        )
    )
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
    instructions(
        methodCall(returnType = "Lcom/google/android/libraries/youtube/player/model/PlaybackStartDescriptor;"),
        // None of these strings are unique.
        string("com.google.android.apps.youtube.app.endpoint.flags"),
        string("ReelWatchFragmentArgs"),
        string("reels_fragment_descriptor")
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
    instructions(
        // None of these strings are unique.
        string("com.google.android.apps.youtube.app.endpoint.flags"),
        string("ReelWatchFragmentArgs"),
        string("reels_fragment_descriptor")
    )
}

internal val exitVideoPlayerFingerprint = fingerprint {
    returns("V")
    parameters()
    instructions(
        resourceLiteral(ResourceType.ID, "mdx_drawer_layout")
    )
}