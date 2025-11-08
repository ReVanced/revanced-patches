package app.revanced.patches.solidexplorer2.functionality.filesize

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val onReadyFingerprint = fingerprint {
    opcodes(
        Opcode.CONST_WIDE_32, // Constant storing the 2MB limit
        Opcode.CMP_LONG,
        Opcode.IF_LEZ,
    )
    custom { method, _ ->
        method.name == "onReady" && method.definingClass == "Lpl/solidexplorer/plugins/texteditor/TextEditor;"
    }
}
