package app.revanced.patches.youtube.interaction.seekbar

import app.revanced.patcher.fingerprint
import app.revanced.util.containsLiteralInstruction
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionReversed
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal val swipingUpGestureParentFingerprint = fingerprint {
    returns("Z")
    parameters()
    literal { 45379021 }
}

/**
 * Resolves using the class found in [swipingUpGestureParentFingerprint].
 */
internal val showSwipingUpGuideFingerprint = fingerprint {
    accessFlags(AccessFlags.FINAL)
    returns("Z")
    parameters()
    literal { 1 }
}

/**
 * Resolves using the class found in [swipingUpGestureParentFingerprint].
 */
internal val allowSwipingUpGestureFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L")
}

internal val disableFastForwardLegacyFingerprint = fingerprint {
    returns("Z")
    parameters()
    opcodes(Opcode.MOVE_RESULT)
    literal { 45411330 }
}

internal val disableFastForwardGestureFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters()
    opcodes(
        Opcode.IF_EQZ,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
    )
    custom { methodDef, classDef ->
        methodDef.implementation!!.instructions.count() > 30 &&
            classDef.type.endsWith("/NextGenWatchLayout;")
    }
}

internal val onTouchEventHandlerFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.PUBLIC)
    returns("Z")
    parameters("L")
    opcodes(
        Opcode.INVOKE_VIRTUAL, // nMethodReference
        Opcode.RETURN,
        Opcode.IGET_OBJECT,
        Opcode.IGET_BOOLEAN,
        Opcode.IF_EQZ,
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN,
        Opcode.INT_TO_FLOAT,
        Opcode.INT_TO_FLOAT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
        Opcode.INVOKE_VIRTUAL,
        Opcode.INVOKE_VIRTUAL, // oMethodReference
    )
    custom { method, _ -> method.name == "onTouchEvent" }
}

internal val seekbarTappingFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters("L")
    custom { method, _ ->
        method.name == "onTouchEvent"
                && method.containsLiteralInstruction(Integer.MAX_VALUE.toLong())
                && indexOfNewPointInstruction(method) >= 0
    }
}

internal fun indexOfNewPointInstruction(method: Method) = method.indexOfFirstInstructionReversed {
    val reference = getReference<MethodReference>()
    reference?.definingClass == "Landroid/graphics/Point;"
            && reference.name == "<init>"
}

internal val slideToSeekFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returns("V")
    parameters("Landroid/view/View;", "F")
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
        Opcode.GOTO_16,
    )
    literal { 67108864 }
}

internal val fullscreenSeekbarThumbnailsQualityFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters()
    literal { 45399684L }
}
