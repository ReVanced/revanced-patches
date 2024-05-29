package app.revanced.patches.youtube.misc.fix.playback.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val buildInitPlaybackRequestFingerprint = methodFingerprint {
    returns("Lorg/chromium/net/UrlRequest\$Builder;")
    opcodes(
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IGET_OBJECT, // Moves the request URI string to a register to build the request with.
    )
    strings(
        "Content-Type",
        "Range",
    )
}
