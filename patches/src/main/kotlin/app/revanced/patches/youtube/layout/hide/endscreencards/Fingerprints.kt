package app.revanced.patches.youtube.layout.hide.endscreencards

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.layoutCircleMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameterTypes()
    returnType("Landroid/view/View;")
    opcodes(
        Opcode.CONST,
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
    )
    literal { layoutCircle }
}

internal val BytecodePatchContext.layoutIconMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameterTypes()
    returnType("Landroid/view/View;")
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,

        )
    literal { layoutIcon }
}

internal val BytecodePatchContext.layoutVideoMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC)
    parameterTypes()
    returnType("Landroid/view/View;")
    opcodes(
        Opcode.CONST,
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
    )
    literal { layoutVideo }
}

internal val BytecodePatchContext.showEndscreenCardsMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("L")
    instructions(
        allOf(Opcode.IPUT_OBJECT(), field { type == "Ljava/lang/String;" }),
        allOf(Opcode.IGET_OBJECT(), field { type == "Ljava/lang/String;" }),
        afterAtMost(7, allOf(Opcode.INVOKE_VIRTUAL(), method("ordinal"))),
        5L(),
        8L(),
        9L()
    )
    custom {
        immutableClassDef.methods.count() == 5
                // 'public final' or 'final'
                && AccessFlags.FINAL.isSet(accessFlags)
    }
}

