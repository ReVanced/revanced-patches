package app.revanced.patches.youtube.layout.hide.endscreencards

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef

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


internal val BytecodePatchContext.showEndscreenCardsParentMethod by gettingFirstImmutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("[L")
    parameterTypes("L")
    instructions(
        Opcode.NEW_ARRAY(),
        afterAtMost(12, 1024L()),
        afterAtMost(12, 1L()),
    )
    custom {
        immutableClassDef.methods.count() == 5
    }
}

/**
 * Matches to the class found in [showEndscreenCardsParentMethod].
 */
context(_: BytecodePatchContext)
internal fun ClassDef.getShowEndscreenCardsMethod() = firstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("L")
    instructions(5L(), 8L(), 9L())
}
