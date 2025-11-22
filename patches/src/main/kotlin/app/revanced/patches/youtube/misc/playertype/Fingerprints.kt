package app.revanced.patches.youtube.misc.playertype

import app.revanced.patcher.InstructionLocation.MatchAfterWithin
import app.revanced.patcher.fingerprint
import app.revanced.patcher.opcode
import app.revanced.patcher.string
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.shared.misc.mapping.resourceLiteral
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val playerTypeEnumFingerprint = fingerprint {
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

internal val reelWatchPagerFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/view/View;")
    instructions(
        resourceLiteral(ResourceType.ID, "reel_watch_player"),
        opcode(Opcode.MOVE_RESULT_OBJECT, location = MatchAfterWithin(10))
    )
}

internal val videoStateEnumFingerprint = fingerprint {
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
    parameters()
    strings(
        "NEW",
        "PLAYING",
        "PAUSED",
        "RECOVERABLE_ERROR",
        "UNRECOVERABLE_ERROR",
        "ENDED"
    )
}

// 20.33 and lower class name ControlsState. 20.34+ class name is obfuscated.
internal val controlsStateToStringFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters()
    returns("Ljava/lang/String;")
    instructions(
        string("videoState"),
        string("isBuffering")
    )
}
