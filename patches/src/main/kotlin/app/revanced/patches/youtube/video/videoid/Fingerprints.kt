package app.revanced.patches.youtube.video.videoid

import app.revanced.patcher.fingerprint
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val videoIdFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L")
    opcodes(
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_OBJECT,
    )
    custom { method, _ ->
        method.indexOfPlayerResponseModelString() >= 0
    }
}

internal val videoIdBackgroundPlayFingerprint = fingerprint {
    accessFlags(AccessFlags.DECLARED_SYNCHRONIZED, AccessFlags.FINAL, AccessFlags.PUBLIC)
    returns("V")
    parameters("L")
    opcodes(
        Opcode.IF_EQZ,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IPUT_OBJECT,
        Opcode.MONITOR_EXIT,
        Opcode.RETURN_VOID,
        Opcode.MONITOR_EXIT,
        Opcode.RETURN_VOID
    )
    // The target snippet of code is buried in a huge switch block and the target method
    // has been changed many times by YT which makes identifying it more difficult than usual.
    custom { method, classDef ->
        // Access flags changed in 19.36
        AccessFlags.FINAL.isSet(method.accessFlags) &&
                AccessFlags.DECLARED_SYNCHRONIZED.isSet(method.accessFlags) &&
                classDef.methods.count() == 17 &&
                method.implementation != null &&
                method.indexOfPlayerResponseModelString() >= 0
    }

}

internal val videoIdParentFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("[L")
    parameters("L")
    literal { 524288L }
}
