package app.revanced.patches.youtube.layout.sponsorblock

import app.revanced.patcher.fingerprint
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionReversed
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal val appendTimeFingerprint by fingerprint {
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
        Opcode.IF_NEZ,
        Opcode.IGET_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN_VOID,
    )
}

internal val controlsOverlayFingerprint by fingerprint {
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

internal val rectangleFieldInvalidatorFingerprint by fingerprint {
    returns("V")
    parameters()
    custom  { method, _ ->
        indexOfInvalidateInstruction(method) >= 0
    }
}

internal val segmentPlaybackControllerFingerprint by fingerprint {
    returns("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    parameters("Ljava/lang/Object;")
    opcodes(Opcode.CONST_STRING)
    custom { method, _ ->
        method.definingClass == EXTENSION_SEGMENT_PLAYBACK_CONTROLLER_CLASS_DESCRIPTOR
                && method.name == "setSponsorBarRect"
    }
}

internal fun indexOfInvalidateInstruction(method: Method) =
    method.indexOfFirstInstructionReversed {
        getReference<MethodReference>()?.name == "invalidate"
    }
