package app.revanced.patches.music.general.floatingbutton.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

object FloatingButtonFingerprint : MethodFingerprint(
    returnType = "V",
    parameters = listOf("L"),
    opcodes = listOf(
        Opcode.IGET,
        Opcode.AND_INT_LIT16,
        Opcode.IF_EQZ
    )
)

