package app.revanced.patches.solidexplorer2.functionality.filesize

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.opcodes
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.onReadyMethod by gettingFirstMutableMethodDeclaratively {
    name("onReady")
    definingClass("Lpl/solidexplorer/plugins/texteditor/TextEditor;")
    opcodes(
        Opcode.CONST_WIDE_32, // Constant storing the 2MB limit
        Opcode.CMP_LONG,
        Opcode.IF_LEZ,
    )
}
