package app.revanced.patches.youtube.misc.fix.playback.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object PlayerResponseModelBackgroundAudioPlaybackFingerprint : MethodFingerprint(
    returnType = "Z",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.STATIC,
    parameters = listOf("Lcom/google/android/libraries/youtube/innertube/model/player/PlayerResponseModel;"),
    opcodes = listOf(
        Opcode.CONST_4,
        Opcode.IF_EQZ,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IF_NEZ,
        Opcode.GOTO,
        Opcode.RETURN,
        null,   // Opcode.CONST_4 or Opcode.MOVE
        Opcode.RETURN,
    )
)