package app.revanced.patches.lightroom.misc.premium.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val hasPurchasedFingerprint = methodFingerprint {
    returns("Z")
    accessFlags(AccessFlags.PRIVATE,AccessFlags.FINAL)
    strings("isPurchaseDoneRecently = true, access platform profile present? = ")
    opcodes(
        Opcode.SGET_OBJECT,
        Opcode.CONST_4,
        Opcode.CONST_4,
        Opcode.CONST_4,
    )
}
