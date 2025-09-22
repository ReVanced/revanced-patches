package app.revanced.patches.youtube.misc.fix.backtoexitgesture

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val scrollPositionFingerprint = fingerprint {
    accessFlags(AccessFlags.PROTECTED, AccessFlags.FINAL)
    returns("V")
    parameters("L")
    opcodes(
        Opcode.IF_NEZ,
        Opcode.INVOKE_DIRECT,
        Opcode.RETURN_VOID
    )
    strings("scroll_position")
}

/**
 * Resolves using class found in [recyclerViewTopScrollingParentFingerprint].
 */
internal val recyclerViewTopScrollingFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters()
    opcodes(
        Opcode.CHECK_CAST,
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL,
        Opcode.GOTO,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_INTERFACE
    )
}

internal val recyclerViewTopScrollingParentFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    opcodes(
        Opcode.IPUT_OBJECT,
        Opcode.IPUT_OBJECT,
        Opcode.IPUT_OBJECT,
        Opcode.IPUT_OBJECT,
        Opcode.CONST_16,
        Opcode.INVOKE_VIRTUAL,
        Opcode.NEW_INSTANCE,
        Opcode.INVOKE_DIRECT,
        Opcode.IPUT_OBJECT,
        Opcode.RETURN_VOID
    )
}
