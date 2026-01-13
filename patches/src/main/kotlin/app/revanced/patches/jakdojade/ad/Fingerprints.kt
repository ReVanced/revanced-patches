package app.revanced.patches.jakdojade.ad

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val isPremiumFingerprint = fingerprint {
    returns("Z")
    accessFlags(AccessFlags.PUBLIC)
    parameters()
    opcodes(
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IF_NEZ,
        Opcode.CONST_4,
        Opcode.GOTO,
        Opcode.CONST_4,
        Opcode.RETURN
    )
}

internal val premiumRenewalDateFingerprint = fingerprint {
    returns("Ljava/lang/String;")
    accessFlags(AccessFlags.PUBLIC)
    parameters()
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
    )
}

internal val getGoogleProductFingerprint = fingerprint {
    returns("Lcom/citynav/jakdojade/pl/android/billing/output/GoogleProduct;")
    accessFlags(AccessFlags.PUBLIC)
    parameters("Lcom/citynav/jakdojade/pl/android/profiles/ui/profile/userprofile/model/PremiumType;")
}