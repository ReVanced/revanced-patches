package app.revanced.patches.solidexplorer2.functionality.filesize.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

object OnReadyFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.CONST_WIDE_32, // Constant storing the 2MB limit
        Opcode.CMP_LONG,
        Opcode.IF_LEZ,
    ),
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass == "Lpl/solidexplorer/plugins/texteditor/TextEditor;" && methodDef.name == "onReady"
    }
)