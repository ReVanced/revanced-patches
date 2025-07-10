package app.revanced.patches.youtube.misc.playertype

import app.revanced.patcher.fingerprint
import app.revanced.patcher.opcode
import app.revanced.patches.shared.misc.mapping.resourceLiteral
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val playerTypeEnumFingerprint by fingerprint {
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

internal val reelWatchPagerFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/view/View;")
    instructions(
        resourceLiteral("id", "reel_watch_player"),
        opcode(Opcode.MOVE_RESULT_OBJECT, maxAfter = 10)
    )
}

internal val videoStateEnumFingerprint by fingerprint {
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
