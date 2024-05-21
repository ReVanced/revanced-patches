package app.revanced.patches.youtube.misc.fix.playback.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal object BuildInitPlaybackRequestFingerprint : MethodFingerprint(
    returnType = "Lorg/chromium/net/UrlRequest\$Builder;",
    opcodes = listOf(
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IGET_OBJECT, // Moves the request URI string to a register to build the request with.
    ),
    strings = listOf(
        "Content-Type",
        "Range",
    ),
)
