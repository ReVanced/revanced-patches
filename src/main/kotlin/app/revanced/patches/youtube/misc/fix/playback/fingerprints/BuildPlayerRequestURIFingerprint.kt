package app.revanced.patches.youtube.misc.fix.playback.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal object BuildPlayerRequestURIFingerprint : MethodFingerprint(
    returnType = "Ljava/lang/String;",
    opcodes = listOf(
        Opcode.INVOKE_VIRTUAL, // Register holds player request URI.
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IPUT_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.MONITOR_EXIT,
        Opcode.RETURN_OBJECT,
    ),
    strings = listOf(
        "youtubei/v1",
        "key",
        "asig",
    ),
)
