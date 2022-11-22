package app.revanced.patches.youtube.layout.autocaptions.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

object SubtitleTrackFingerprint : MethodFingerprint(
    "Z", AccessFlags.PUBLIC or AccessFlags.FINAL, listOf(), listOf(
        Opcode.CONST_STRING,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.RETURN,
    ),
    strings = listOf("DISABLE_CAPTIONS_OPTION")
)