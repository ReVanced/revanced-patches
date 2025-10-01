package app.revanced.patches.youtube.layout.hide.endscreencards

import app.revanced.patcher.fingerprint
import app.revanced.util.containsLiteralInstruction
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

internal val layoutCircleFingerprint = fingerprint {
    returns("Landroid/view/View;")
    opcodes(
        Opcode.CONST,
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
    )
    literal { layoutCircle }
}

internal val layoutIconFingerprint = fingerprint {
    returns("Landroid/view/View;")
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,

    )
    literal { layoutIcon }
}

internal val layoutVideoFingerprint = fingerprint {
    returns("Landroid/view/View;")
    opcodes(
        Opcode.CONST,
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
    )
    literal { layoutVideo }
}

internal val showEndscreenCardsFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L")
    custom { method, classDef ->
        classDef.methods.count() == 5
                && method.containsLiteralInstruction(0)
                && method.containsLiteralInstruction(5)
                && method.containsLiteralInstruction(8)
                && method.indexOfFirstInstruction {
            val reference = getReference<FieldReference>()
            reference?.type == "Lcom/google/android/libraries/youtube/innertube/model/player/PlayerResponseModel;"
        } >= 0
    }
}