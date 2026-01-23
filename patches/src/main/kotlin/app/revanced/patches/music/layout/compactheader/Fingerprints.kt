package app.revanced.patches.music.layout.compactheader

import app.revanced.patcher.fingerprint
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.Opcode

internal val chipCloudFingerprint = fingerprint {
    returnType("V")
    opcodes(
        Opcode.CONST,
        Opcode.CONST_4,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
    )
    literal { chipCloud }
}
