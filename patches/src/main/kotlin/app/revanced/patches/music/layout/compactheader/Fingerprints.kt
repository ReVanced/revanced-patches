package app.revanced.patches.music.layout.compactheader

import com.android.tools.smali.dexlib2.Opcode
import app.revanced.patcher.fingerprint
import app.revanced.util.literal

internal val chipCloudFingerprint = fingerprint {
    returns("V")
    opcodes(
        Opcode.CONST,
        Opcode.CONST_4,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT
    )
    literal { chipCloud }
}
