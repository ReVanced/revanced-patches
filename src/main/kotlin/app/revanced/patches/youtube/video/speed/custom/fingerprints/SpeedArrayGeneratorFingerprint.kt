package app.revanced.patches.youtube.video.speed.custom.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object SpeedArrayGeneratorFingerprint : MethodFingerprint(
    returnType = "[L",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.STATIC,
    parameters = listOf("Lcom/google/android/libraries/youtube/innertube/model/player/PlayerResponseModel;"),
    opcodes = listOf(
        Opcode.IF_NEZ,
        Opcode.SGET_OBJECT,
        Opcode.GOTO_16,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IGET_OBJECT,
    ),
    strings = listOf("0.0#")
)
