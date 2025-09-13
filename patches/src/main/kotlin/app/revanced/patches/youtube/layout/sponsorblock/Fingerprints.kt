package app.revanced.patches.youtube.layout.sponsorblock

import app.revanced.patcher.fingerprint
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionReversed
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal val appendTimeFingerprint = fingerprint {
    returns("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters("Ljava/lang/CharSequence;", "Ljava/lang/CharSequence;", "Ljava/lang/CharSequence;")
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT,
    )
}

internal val controlsOverlayFingerprint = fingerprint {
    returns("V")
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    parameters()
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST, // R.id.inset_overlay_view_layout
        Opcode.IPUT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.CONST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.NEW_INSTANCE,
    )
}

internal val rectangleFieldInvalidatorFingerprint = fingerprint {
    returns("V")
    custom { method, _ ->
        val instructions = method.implementation?.instructions!!
        val instructionCount = instructions.count()

        // the method has definitely more than 5 instructions
        if (instructionCount < 5) return@custom false

        val referenceInstruction = instructions.elementAt(instructionCount - 2) // the second to last instruction
        val reference = ((referenceInstruction as? ReferenceInstruction)?.reference as? MethodReference)

        reference?.parameterTypes?.size == 1 && reference.name == "invalidate" // the reference is the invalidate(..) method
    }
}

internal val adProgressTextViewVisibilityFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Z")
    custom { method, _ ->
        indexOfAdProgressTextViewVisibilityInstruction(method) >= 0
    }
}

internal fun indexOfAdProgressTextViewVisibilityInstruction(method: Method) =
    method.indexOfFirstInstructionReversed {
        val reference = getReference<MethodReference>()
        reference?.definingClass ==
                "Lcom/google/android/libraries/youtube/ads/player/ui/AdProgressTextView;"
                && reference.name =="setVisibility"
    }
