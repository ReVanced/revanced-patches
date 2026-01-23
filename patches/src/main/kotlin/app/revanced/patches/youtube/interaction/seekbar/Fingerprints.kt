package app.revanced.patches.youtube.interaction.seekbar

import app.revanced.patcher.InstructionLocation.MatchAfterImmediately
import app.revanced.patcher.InstructionLocation.MatchAfterWithin
import app.revanced.patcher.accessFlags
import app.revanced.patcher.fieldAccess
import app.revanced.patcher.fingerprint
import app.revanced.patcher.firstMethodComposite
import app.revanced.patcher.firstMutableMethodDeclaratively
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.methodCall
import app.revanced.patcher.newInstance
import app.revanced.patcher.opcode
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.patches.youtube.misc.playservice.is_19_34_or_greater
import app.revanced.patches.youtube.misc.playservice.is_19_47_or_greater
import app.revanced.patches.youtube.misc.playservice.is_20_19_or_greater
import app.revanced.patches.youtube.misc.playservice.is_20_20_or_greater
import app.revanced.patches.youtube.misc.playservice.is_20_31_or_greater
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.reference.StringReference

internal val BytecodePatchContext.swipingUpGestureParentMethod by gettingFirstMethodDeclaratively {
    returnType("Z")
    parameterTypes()
    instructions(
        45379021L(), // Swipe up fullscreen feature flag
    )
}

/**
 * Resolves using the class found in [swipingUpGestureParentMethod].
 */
context(_: BytecodePatchContext)
internal fun ClassDef.getShowSwipingUpGuideMethod() = firstMutableMethodDeclaratively {
    accessFlags(AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
    instructions(1L())
}

/**
 * Resolves using the class found in [swipingUpGestureParentMethod].
 */
context(_: BytecodePatchContext)
internal fun ClassDef.getAllowSwipingUpGestureMethod() = firstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("L")
}

internal val disableFastForwardLegacyFingerprint = fingerprint {
    returnType("Z")
    parameterTypes()
    opcodes(Opcode.MOVE_RESULT)
    // Intent start flag only used in the subscription activity
    literal { 45411330 }
}

internal val disableFastForwardGestureFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
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

internal val customTapAndHoldFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes()
    instructions(
        2.0f(),
    )
    custom { method, _ ->
        // Code is found in different methods with different strings.
        val findSearchLandingKey = (is_19_34_or_greater && !is_19_47_or_greater) ||
            (is_20_19_or_greater && !is_20_20_or_greater) || is_20_31_or_greater

        method.name == "run" && method.indexOfFirstInstruction {
            val string = getReference<StringReference>()?.string
            string == "Failed to easy seek haptics vibrate." ||
                (findSearchLandingKey && string == "search_landing_cache_key")
        } >= 0
    }
}

internal val onTouchEventHandlerFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.PUBLIC)
    returnType("Z")
    parameterTypes("L")
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
    returnType("Z")
    parameterTypes("Landroid/view/MotionEvent;")
    instructions(
        Int.MAX_VALUE(),

        newInstance("Landroid/graphics/Point;"),
        methodCall(smali = "Landroid/graphics/Point;-><init>(II)V", location = MatchAfterImmediately()),
        methodCall(
            smali = "Lj\$/util/Optional;->of(Ljava/lang/Object;)Lj\$/util/Optional;",
            location = MatchAfterImmediately(),
        ),
        opcode(Opcode.MOVE_RESULT_OBJECT, location = MatchAfterImmediately()),
        fieldAccess(opcode = Opcode.IPUT_OBJECT, type = "Lj\$/util/Optional;", location = MatchAfterImmediately()),

        opcode(Opcode.INVOKE_VIRTUAL, location = MatchAfterWithin(10)),
    )
    custom { method, _ -> method.name == "onTouchEvent" }
}

internal val slideToSeekFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("Landroid/view/View;", "F")
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
        Opcode.GOTO_16,
    )
    literal { 67108864 }
}

internal val BytecodePatchContext.fullscreenSeekbarThumbnailsQualityMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
    instructions(
        45399684L(), // Video stream seekbar thumbnails feature flag.
    )
}

internal val fullscreenLargeSeekbarFeatureFlagMethodMatch = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
    instructions(45691569L())
}
