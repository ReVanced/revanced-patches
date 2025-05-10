package app.revanced.patches.messenger.navbar

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val createTabConfigurationFingerprint by fingerprint {
    opcodes(
        Opcode.INVOKE_DIRECT,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
        Opcode.INVOKE_DIRECT,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
    )
    strings("MessengerTabConfigurationCreator.createTabConfiguration")
}
