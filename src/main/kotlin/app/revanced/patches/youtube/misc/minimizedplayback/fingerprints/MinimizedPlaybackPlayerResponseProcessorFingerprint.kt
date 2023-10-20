package app.revanced.patches.youtube.misc.minimizedplayback.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object MinimizedPlaybackPlayerResponseProcessorFingerprint : MethodFingerprint(
    "Z",
    AccessFlags.PUBLIC or AccessFlags.FINAL,
    listOf("Lcom/google/android/libraries/youtube/innertube/model/player/PlayerResponseModel;", "I"),
    listOf(
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.CONST_4,
        Opcode.IF_EQZ,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT,
        Opcode.CONST_4,
        Opcode.IF_NEZ,
        Opcode.CONST_4,
        Opcode.IF_EQ,
        Opcode.GOTO,
        Opcode.RETURN,
        Opcode.CONST_4,
        Opcode.RETURN
    )
)