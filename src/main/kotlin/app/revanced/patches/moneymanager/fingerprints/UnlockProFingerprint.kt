package app.revanced.patches.moneymanager.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val unlockProFingerprint = methodFingerprint {
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