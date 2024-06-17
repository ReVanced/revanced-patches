package app.revanced.patches.moneymanager

import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint

internal val unlockProFingerprint = fingerprint {
    accessFlags(AccessFlags.STATIC, AccessFlags.SYNTHETIC)
    returns("Z")
    parameters("L")
    opcodes(
        Opcode.IGET_BOOLEAN,
        Opcode.RETURN
    )
    custom { _, classDef ->
        classDef.endsWith("MainActivity;")
    }
}