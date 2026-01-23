package app.revanced.patches.youtube.misc.playercontrols

import app.revanced.patcher.InstructionLocation.MatchAfterImmediately
import app.revanced.patcher.accessFlags
import app.revanced.patcher.checkCast
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.literal
import app.revanced.patcher.methodCall
import app.revanced.patcher.opcode
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.patches.shared.misc.mapping.ResourceType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.playerControlsVisibilityEntityModelMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC)
    returnType("L")
    parameterTypes()
    opcodes(
        Opcode.IGET,
        Opcode.INVOKE_STATIC,
    )
    custom { method, _ ->
        method.name == "getPlayerControlsVisibility"
    }
}

internal val BytecodePatchContext.youtubeControlsOverlayMethod by gettingFirstMethodDeclaratively {
    returnType("V")
    parameterTypes()
    instructions(
        methodCall(name = "setFocusableInTouchMode"),
        ResourceType.ID("inset_overlay_view_layout"),
        ResourceType.ID("scrim_overlay"),
    )
}

internal val BytecodePatchContext.motionEventMethod by gettingFirstMethodDeclaratively {
    returnType("V")
    parameterTypes("Landroid/view/MotionEvent;")
    instructions(
        methodCall(name = "setTranslationY"),
    )
}

internal val BytecodePatchContext.playerControlsExtensionHookListenersExistMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returnType("Z")
    parameterTypes()
    custom { methodDef, classDef ->
        methodDef.name == "fullscreenButtonVisibilityCallbacksExist" &&
            classDef.type == EXTENSION_CLASS_DESCRIPTOR
    }
}

internal val BytecodePatchContext.playerControlsExtensionHookMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returnType("V")
    parameterTypes("Z")
    custom { methodDef, classDef ->
        methodDef.name == "fullscreenButtonVisibilityChanged" &&
            classDef.type == EXTENSION_CLASS_DESCRIPTOR
    }
}

internal val BytecodePatchContext.playerTopControlsInflateMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes()
    instructions(
        ResourceType.ID("controls_layout_stub"),
        methodCall("Landroid/view/ViewStub;", "inflate"),
        opcode(Opcode.MOVE_RESULT_OBJECT, MatchAfterImmediately()),
    )
}

internal val BytecodePatchContext.playerBottomControlsInflateMethod by gettingFirstMethodDeclaratively {
    returnType("Ljava/lang/Object;")
    parameterTypes()
    instructions(
        ResourceType.ID("bottom_ui_container_stub"),
        methodCall("Landroid/view/ViewStub;", "inflate"),
        opcode(Opcode.MOVE_RESULT_OBJECT, MatchAfterImmediately()),
    )
}

internal val BytecodePatchContext.overlayViewInflateMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("Landroid/view/View;")
    instructions(
        ResourceType.ID("heatseeker_viewstub"),
        ResourceType.ID("fullscreen_button"),
        checkCast("Landroid/widget/ImageView;"),
    )
}

/**
 * Resolves to the class found in [playerTopControlsInflateMethod].
 */
internal val BytecodePatchContext.controlsOverlayVisibilityMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("Z", "Z")
}

internal val BytecodePatchContext.playerBottomControlsExploderFeatureFlagMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
    instructions(
        45643739L(),
    )
}

internal val BytecodePatchContext.playerTopControlsExperimentalLayoutFeatureFlagMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("I")
    parameterTypes()
    instructions(
        45629424L(),
    )
}

internal val BytecodePatchContext.playerControlsLargeOverlayButtonsFeatureFlagMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
    instructions(
        45709810L(),
    )
}

internal val BytecodePatchContext.playerControlsFullscreenLargeButtonsFeatureFlagMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
    instructions(
        45686474L(),
    )
}

internal val BytecodePatchContext.playerControlsButtonStrokeFeatureFlagMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
    instructions(
        45713296L(),
    )
}
