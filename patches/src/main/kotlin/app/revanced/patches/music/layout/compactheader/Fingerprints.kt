package app.revanced.patches.music.layout.compactheader

import app.revanced.patcher.*
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.Opcode

internal val chipCloudMethodMatch = firstMethodComposite {
    returnType("V")
    opcodes(
        Opcode.CONST,
        Opcode.CONST_4,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
    )
    literal { chipCloud }
}
