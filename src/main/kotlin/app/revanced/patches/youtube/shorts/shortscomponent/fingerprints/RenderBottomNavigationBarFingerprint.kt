package app.revanced.patches.youtube.shorts.shortscomponent.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

object RenderBottomNavigationBarFingerprint : MethodFingerprint(
    returnType = "V",
    opcodes = listOf(
        Opcode.CHECK_CAST,
        Opcode.IGET_OBJECT,
        Opcode.CONST_STRING,
        Opcode.INVOKE_VIRTUAL
    ),
    strings = listOf("r_as")
)