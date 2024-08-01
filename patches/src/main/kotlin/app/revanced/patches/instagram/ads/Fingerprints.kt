package app.revanced.patches.instagram.ads

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val isAdCheckOneFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters()
    opcodes(
        Opcode.XOR_INT_LIT8,
        Opcode.IF_NE,
        Opcode.RETURN,
        Opcode.INVOKE_VIRTUAL,
    )
}

internal val isAdCheckTwoFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("Z")
    parameters("L")
    opcodes(
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST_4,
        Opcode.IF_EQZ,
        Opcode.CONST_4,
        Opcode.RETURN,
    )
}

internal val showAdFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL)
    returns("Z")
    parameters("L", "L", "Z", "Z")
    opcodes(
        Opcode.SGET_OBJECT,
        Opcode.IF_NE,
        Opcode.IF_NEZ,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT,
        Opcode.IF_NEZ,
        Opcode.RETURN,
    )
}

internal val adInjectorFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE)
    returns("Z")
    parameters("L", "L")
    strings(
        "SponsoredContentController.insertItem",
        "SponsoredContentController::Delivery",
    )
}
