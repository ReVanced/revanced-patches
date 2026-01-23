package app.revanced.patches.youtube.layout.shortsplayer

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patches.shared.misc.mapping.ResourceType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * Purpose of this method is not clear, and it's only used to identify
 * the obfuscated name of the videoId() method in PlaybackStartDescriptor.
 * 20.38 and lower.
 */
internal val playbackStartFeatureFlagMethodMatch = firstMethodComposite {
    returnType("Z")
    parameterTypes("Lcom/google/android/libraries/youtube/player/model/PlaybackStartDescriptor;")
    instructions(
        method {
            definingClass == "Lcom/google/android/libraries/youtube/player/model/PlaybackStartDescriptor;" &&
                returnType == "Ljava/lang/String;"
        },
        45380134L(),
    )
}

/**
 * Purpose of this method is not entirely clear, and it's only used to identify
 * the obfuscated name of the videoId() method in PlaybackStartDescriptor.
 * 20.39+
 */
internal val watchPanelVideoIdMethodMatch = firstMethodComposite {
    returnType("Ljava/lang/String;")
    parameterTypes()
    instructions(
        allOf(
            Opcode.IGET_OBJECT(),
            field { type == "Lcom/google/android/apps/youtube/app/common/player/queue/WatchPanelId;" },
        ),
        allOf(
            Opcode.CHECK_CAST(),
            type("Lcom/google/android/apps/youtube/app/common/player/queue/DefaultWatchPanelId;"),
        ),
        method {
            definingClass == "Lcom/google/android/apps/youtube/app/common/player/queue/DefaultWatchPanelId;" &&
                returnType == "Lcom/google/android/libraries/youtube/player/model/PlaybackStartDescriptor;"
        },
        method {
            definingClass == "Lcom/google/android/libraries/youtube/player/model/PlaybackStartDescriptor;" &&
                returnType == "Ljava/lang/String;"
        },

    )
}

// Pre 19.25
internal val shortsPlaybackIntentLegacyMethodMatch = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes(
        "L",
        "Ljava/util/Map;",
        "J",
        "Ljava/lang/String;",
        "Z",
        "Ljava/util/Map;",
    )
    instructions(
        method { returnType == "Lcom/google/android/libraries/youtube/player/model/PlaybackStartDescriptor;" },
        // None of these strings are unique.
        "com.google.android.apps.youtube.app.endpoint.flags"(),
        "ReelWatchFragmentArgs"(),
        "reels_fragment_descriptor"(),
    )
}

internal val BytecodePatchContext.shortsPlaybackIntentMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PROTECTED, AccessFlags.FINAL)
    returnType("V")
    parameterTypes(
        "Lcom/google/android/libraries/youtube/player/model/PlaybackStartDescriptor;",
        "Ljava/util/Map;",
        "J",
        "Ljava/lang/String;",
    )
    instructions(
        // None of these strings are unique.
        "com.google.android.apps.youtube.app.endpoint.flags"(),
        "ReelWatchFragmentArgs"(),
        "reels_fragment_descriptor"(),
    )
}

internal val BytecodePatchContext.exitVideoPlayerMethod by gettingFirstMutableMethodDeclaratively {
    returnType("V")
    parameterTypes()
    instructions(ResourceType.ID("mdx_drawer_layout"))
}
