package app.revanced.patches.youtube.video.videoid.fingerprint

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patches.youtube.video.videoid.VideoIdPatch.indexOfPlayerResponseModelString
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object VideoIdBackgroundPlayFingerprint : MethodFingerprint(
    returnType = "V",
    parameters = listOf("L"),
    opcodes = listOf(
        Opcode.IF_EQZ,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IPUT_OBJECT,
        Opcode.MONITOR_EXIT,
        Opcode.RETURN_VOID,
        Opcode.MONITOR_EXIT,
        Opcode.RETURN_VOID
    ),
    customFingerprint = { methodDef, classDef ->
        // Access flags changed in 19.36
        (methodDef.accessFlags == (AccessFlags.PUBLIC or AccessFlags.FINAL or AccessFlags.DECLARED_SYNCHRONIZED) ||
            methodDef.accessFlags == (AccessFlags.FINAL or AccessFlags.DECLARED_SYNCHRONIZED)) &&
                classDef.methods.count() == 17 &&
                methodDef.implementation != null &&
                methodDef.indexOfPlayerResponseModelString() >= 0
    }
)
