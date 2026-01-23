package app.revanced.patches.music.layout.compactheader

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.opcodes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.chipCloudMethod by gettingFirstMethodDeclaratively {
    returnType("V")
    opcodes(
        Opcode.CONST,
        Opcode.CONST_4,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
    )
    literal { chipCloud }
}
