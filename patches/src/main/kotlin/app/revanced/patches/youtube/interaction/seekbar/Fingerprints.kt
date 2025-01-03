package app.revanced.patches.youtube.interaction.seekbar

import app.revanced.patcher.LiteralFilter
import app.revanced.patcher.fingerprint
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.StringReference

internal val swipingUpGestureParentFingerprint by fingerprint {
    returns("Z")
    parameters()
    literal { 45379021 }
}

/**
 * Resolves using the class found in [swipingUpGestureParentFingerprint].
 */
internal val showSwipingUpGuideFingerprint by fingerprint {
    accessFlags(AccessFlags.FINAL)
    returns("Z")
    parameters()
    literal { 1 }
}

/**
 * Resolves using the class found in [swipingUpGestureParentFingerprint].
 */
internal val allowSwipingUpGestureFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L")
}

internal val disableFastForwardLegacyFingerprint by fingerprint {
    returns("Z")
    parameters()
    opcodes(Opcode.MOVE_RESULT)
    literal { 45411330 }
}

internal val disableFastForwardGestureFingerprint by fingerprint {
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

internal val disableFastForwardNoticeFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters()
    opcodes(
        Opcode.CHECK_CAST,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
    )
    custom { method, _ ->
        method.name == "run" && method.indexOfFirstInstruction {
            // In later targets the code is found in different methods with different strings.
            val string = getReference<StringReference>()?.string
            string == "Failed to easy seek haptics vibrate." || string == "search_landing_cache_key"
        } >= 0
    }
}

internal val onTouchEventHandlerFingerprint by fingerprint {
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

internal val seekbarTappingFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters("L")
    opcodes(
        Opcode.IPUT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        // Insert seekbar tapping instructions here.
        Opcode.RETURN,
        Opcode.INVOKE_VIRTUAL,
    )
    literal { Integer.MAX_VALUE.toLong() }
}

internal val slideToSeekFingerprint by fingerprint {
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

internal val fullscreenSeekbarThumbnailsQualityFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters()
    instructions (
        LiteralFilter(45399684L)
    )
}
