package app.revanced.patches.youtube.layout.hide.shorts.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val renderBottomNavigationBarFingerprint = methodFingerprint {
    opcodes(
        Opcode.IGET_OBJECT,
        Opcode.MONITOR_ENTER,
        Opcode.IGET_OBJECT,
        Opcode.IF_EQZ,
        Opcode.INVOKE_INTERFACE,
        Opcode.MONITOR_EXIT,
        Opcode.RETURN_VOID,
        Opcode.MOVE_EXCEPTION,
        Opcode.MONITOR_EXIT,
        Opcode.THROW,
    )
}
