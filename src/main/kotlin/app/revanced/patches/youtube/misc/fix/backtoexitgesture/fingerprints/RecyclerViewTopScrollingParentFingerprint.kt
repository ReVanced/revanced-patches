package app.revanced.patches.youtube.misc.fix.backtoexitgesture.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val recyclerViewTopScrollingParentFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters("L", "L", "Landroid/view/ViewGroup;", "Landroid/view/ViewGroup;")
    opcodes(
        Opcode.INVOKE_DIRECT,
        Opcode.IPUT_OBJECT,
        Opcode.IPUT_OBJECT,
        Opcode.IPUT_OBJECT,
        Opcode.IPUT_OBJECT,
        Opcode.CONST_16,
        Opcode.INVOKE_VIRTUAL,
        Opcode.NEW_INSTANCE,
    )
}
