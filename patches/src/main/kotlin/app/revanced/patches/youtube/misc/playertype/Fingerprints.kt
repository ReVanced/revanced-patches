package app.revanced.patches.youtube.misc.playertype

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patches.shared.misc.mapping.ResourceType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.playerTypeEnumMethod by gettingFirstImmutableMethodDeclaratively(
    "NONE",
    "HIDDEN",
    "WATCH_WHILE_MINIMIZED",
    "WATCH_WHILE_MAXIMIZED",
    "WATCH_WHILE_FULLSCREEN",
    "WATCH_WHILE_SLIDING_MAXIMIZED_FULLSCREEN",
    "WATCH_WHILE_SLIDING_MINIMIZED_MAXIMIZED",
    "WATCH_WHILE_SLIDING_MINIMIZED_DISMISSED",
    "INLINE_MINIMAL",
    "VIRTUAL_REALITY_FULLSCREEN",
    "WATCH_WHILE_PICTURE_IN_PICTURE",
) {
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
}

internal val BytecodePatchContext.reelWatchPagerMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Landroid/view/View;")
    instructions(
        ResourceType.ID("reel_watch_player"),
        afterAtMost(10, Opcode.MOVE_RESULT_OBJECT()),
    )
}

internal val BytecodePatchContext.videoStateEnumMethod by gettingFirstImmutableMethodDeclaratively(
    "NEW",
    "PLAYING",
    "PAUSED",
    "RECOVERABLE_ERROR",
    "UNRECOVERABLE_ERROR",
    "ENDED",
) {
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
    parameterTypes()
}

// 20.33 and lower class name ControlsState. 20.34+ class name is obfuscated.
internal val BytecodePatchContext.controlsStateToStringMethod by gettingFirstImmutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameterTypes()
    returnType("Ljava/lang/String;")
    instructions(
        "videoState"(),
        "isBuffering"(),
    )
}
