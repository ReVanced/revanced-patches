package app.revanced.patches.music.layout.compactheader

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.chipCloudMethodMatch by composingFirstMethod {
    returnType("V")
    opcodes(
        Opcode.CONST,
        Opcode.CONST_4,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
    )
    literal { chipCloud }
}
