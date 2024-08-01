package app.revanced.patches.yuka.misc.unlockpremium

import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint

internal val isPremiumFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    opcodes(
        Opcode.IGET_BOOLEAN,
        Opcode.RETURN,
    )
}

internal val yukaUserConstructorFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returns("V")
    strings("premiumProvider")
}
