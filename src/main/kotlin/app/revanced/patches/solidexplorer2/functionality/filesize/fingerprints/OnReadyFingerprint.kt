package app.revanced.patches.solidexplorer2.functionality.filesize.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val onReadyFingerprint = methodFingerprint {
    opcodes(
        Opcode.CONST_WIDE_32, // Constant storing the 2MB limit
        Opcode.CMP_LONG,
        Opcode.IF_LEZ,
    )
    custom { methodDef, classDef ->
        classDef == "Lpl/solidexplorer/plugins/texteditor/TextEditor;" && methodDef.name == "onReady"
    }
}