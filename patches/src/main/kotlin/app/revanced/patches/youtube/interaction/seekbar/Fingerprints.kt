package app.revanced.patches.youtube.interaction.seekbar

import app.revanced.patcher.*
import app.revanced.patcher.extensions.instructions
import app.revanced.patcher.extensions.stringReference
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patches.youtube.misc.playservice.*
import app.revanced.util.indexOfFirstInstruction
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef

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

internal val BytecodePatchContext.disableFastForwardLegacyMethodMatch by composingFirstMethod {
    returnType("Z")
    parameterTypes()
    opcodes(Opcode.MOVE_RESULT)
    // Intent start flag only used in the subscription activity
    literal { 45411330 }
}

internal val BytecodePatchContext.disableFastForwardGestureMethodMatch by composingFirstMethod {
    definingClass { endsWith("/NextGenWatchLayout;") }
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
    opcodes(
        Opcode.IF_EQZ,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
    )
    custom { instructions.count() > 30 }
}

internal val BytecodePatchContext.customTapAndHoldMethodMatch by composingFirstMethod {
    name("run")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes()
    instructions(2.0f.toRawBits().toLong()())
    custom {
        // Code is found in different methods with different strings.
        val findSearchLandingKey = (is_19_34_or_greater && !is_19_47_or_greater) ||
            (is_20_19_or_greater && !is_20_20_or_greater) || is_20_31_or_greater

        indexOfFirstInstruction {
            val string = stringReference?.string
            string == "Failed to easy seek haptics vibrate." ||
                (findSearchLandingKey && string == "search_landing_cache_key")
        } >= 0
    }
}

internal val BytecodePatchContext.onTouchEventHandlerMethodMatch by composingFirstMethod {
    name("onTouchEvent")
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
}

internal val BytecodePatchContext.seekbarTappingMethodMatch by composingFirstMethod {
    name("onTouchEvent")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes("Landroid/view/MotionEvent;")
    instructions(
        Int.MAX_VALUE.toLong()(),
        allOf(Opcode.NEW_INSTANCE(), type("Landroid/graphics/Point;")),
        after(method { toString() == "Landroid/graphics/Point;-><init>(II)V" }),
        after(method { toString() == "Lj$/util/Optional;->of(Ljava/lang/Object;)Lj$/util/Optional;" }),
        after(Opcode.MOVE_RESULT_OBJECT()),
        after(allOf(Opcode.IPUT_OBJECT(), field { type == "Lj$/util/Optional;" })),
        afterAtMost(10, Opcode.INVOKE_VIRTUAL()),
    )
}

internal val BytecodePatchContext.slideToSeekMethodMatch by composingFirstMethod {
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

internal val BytecodePatchContext.fullscreenLargeSeekbarFeatureFlagMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
    instructions(45691569L())
}
