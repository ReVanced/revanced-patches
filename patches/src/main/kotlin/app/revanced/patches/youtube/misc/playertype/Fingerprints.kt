package app.revanced.patches.youtube.misc.playertype

import app.revanced.patcher.InstructionLocation.MatchAfterWithin
import app.revanced.patcher.accessFlags
import app.revanced.patcher.addString
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.opcode
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.patches.shared.misc.mapping.ResourceType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.playerTypeEnumMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
    strings(
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
    )
}

internal val BytecodePatchContext.reelWatchPagerMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Landroid/view/View;")
    instructions(
        ResourceType.ID("reel_watch_player"),
        opcode(Opcode.MOVE_RESULT_OBJECT, location = MatchAfterWithin(10)),
    )
}

internal val BytecodePatchContext.videoStateEnumMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
    parameterTypes()
    strings(
        "NEW",
        "PLAYING",
        "PAUSED",
        "RECOVERABLE_ERROR",
        "UNRECOVERABLE_ERROR",
        "ENDED",
    )
}

// 20.33 and lower class name ControlsState. 20.34+ class name is obfuscated.
internal val BytecodePatchContext.controlsStateToStringMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameterTypes()
    returnType("Ljava/lang/String;")
    instructions(
        addString("videoState"),
        addString("isBuffering"),
    )
}
