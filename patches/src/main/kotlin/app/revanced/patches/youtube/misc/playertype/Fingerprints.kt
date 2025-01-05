package app.revanced.patches.youtube.misc.playertype

import app.revanced.patcher.FieldCallFilter
import app.revanced.patcher.LiteralFilter
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val playerTypeFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L")
    opcodes(
        Opcode.IF_NE,
        Opcode.RETURN_VOID,
    )
    custom { _, classDef -> classDef.endsWith("/YouTubePlayerOverlaysLayout;") }
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

internal val videoStateFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Lcom/google/android/libraries/youtube/player/features/overlay/controls/ControlsState;")
    instructions(
        LiteralFilter(1),
        LiteralFilter(literal = 0, maxInstructionsBefore = 10),
        // Obfuscated parameter field name.
        FieldCallFilter(
            definingClass = { "Lcom/google/android/libraries/youtube/player/features/overlay/controls/ControlsState;"},
            type = { context: BytecodePatchContext -> with(context) { videoStateEnumFingerprint.originalClassDef.type } },
            maxInstructionsBefore = 5
        )
    )
}
