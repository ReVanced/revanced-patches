package app.revanced.patches.yuka.misc.unlockpremium

import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint
import app.revanced.patcher.string

internal val isPremiumFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    opcodes(
        Opcode.IGET_BOOLEAN,
        Opcode.RETURN,
    )
}

internal val yukaUserConstructorFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    instructions(
        string("premiumProvider"),
    )
}
