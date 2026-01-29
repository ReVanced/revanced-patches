package app.revanced.patches.youtube.layout.hide.endscreencards

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.util.containsLiteralInstruction
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

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

internal val BytecodePatchContext.showEndscreenCardsMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("L")
    custom {
        immutableClassDef.methods.count() == 5 &&
            containsLiteralInstruction(0) &&
            containsLiteralInstruction(5) &&
            containsLiteralInstruction(8) &&
            indexOfFirstInstruction {
                val reference = getReference<FieldReference>()
                reference?.type == "Lcom/google/android/libraries/youtube/innertube/model/player/PlayerResponseModel;"
            } >= 0
    }
}
