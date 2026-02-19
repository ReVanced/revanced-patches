package app.revanced.patches.youtube.misc.playercontrols

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patches.shared.misc.mapping.ResourceType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef

internal val BytecodePatchContext.playerControlsVisibilityEntityModelMethodMatch by composingFirstMethod {
    name("getPlayerControlsVisibility")
    accessFlags(AccessFlags.PUBLIC)
    returnType("L")
    parameterTypes()
    opcodes(
        Opcode.IGET,
        Opcode.INVOKE_STATIC,
    )
}

internal val BytecodePatchContext.youtubeControlsOverlayMethod by gettingFirstImmutableMethodDeclaratively {
    returnType("V")
    parameterTypes()
    instructions(
        method("setFocusableInTouchMode"),
        ResourceType.ID("inset_overlay_view_layout"),
        ResourceType.ID("scrim_overlay"),
    )
}

internal val ClassDef.motionEventMethodMatch by ClassDefComposing.composingFirstMethod {
    returnType("V")
    parameterTypes("Landroid/view/MotionEvent;")
    instructions(method("setTranslationY"))
}

internal val BytecodePatchContext.playerControlsExtensionHookListenersExistMethod by gettingFirstMethodDeclaratively {
    name("fullscreenButtonVisibilityCallbacksExist")
    definingClass(EXTENSION_CLASS_DESCRIPTOR)
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returnType("Z")
    parameterTypes()
}

internal val BytecodePatchContext.playerControlsExtensionHookMethod by gettingFirstMethodDeclaratively {
    name("fullscreenButtonVisibilityChanged")
    definingClass(EXTENSION_CLASS_DESCRIPTOR)
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returnType("V")
    parameterTypes("Z")
}

internal val BytecodePatchContext.playerTopControlsInflateMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes()
    instructions(
        ResourceType.ID("controls_layout_stub"),
        method { name == "inflate" && definingClass == "Landroid/view/ViewStub;" },
        after(Opcode.MOVE_RESULT_OBJECT()),
    )
}

internal val BytecodePatchContext.playerBottomControlsInflateMethodMatch by composingFirstMethod {
    returnType("Ljava/lang/Object;")
    parameterTypes()
    instructions(
        ResourceType.ID("bottom_ui_container_stub"),
        method { name == "inflate" && definingClass == "Landroid/view/ViewStub;" },
        after(Opcode.MOVE_RESULT_OBJECT()),
    )
}

internal val BytecodePatchContext.overlayViewInflateMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("Landroid/view/View;")
    instructions(
        ResourceType.ID("heatseeker_viewstub"),
        ResourceType.ID("fullscreen_button"),
        allOf(Opcode.CHECK_CAST(), type("Landroid/widget/ImageView;")),
    )
}

/**
 * Resolves to the class found in [playerTopControlsInflateMethodMatch].
 */
context(_: BytecodePatchContext)
internal fun ClassDef.getControlsOverlayVisibilityMethod() = firstMethodDeclaratively {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("Z", "Z")
}

internal val BytecodePatchContext.playerBottomControlsExploderFeatureFlagMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
    instructions(45643739L())
}

internal val BytecodePatchContext.playerTopControlsExperimentalLayoutFeatureFlagMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("I")
    parameterTypes()
    instructions(45629424L())
}

internal val BytecodePatchContext.playerControlsLargeOverlayButtonsFeatureFlagMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
    instructions(45709810L())
}

internal val BytecodePatchContext.playerControlsFullscreenLargeButtonsFeatureFlagMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
    instructions(45686474L())
}

internal val BytecodePatchContext.playerControlsButtonStrokeFeatureFlagMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
    instructions(45713296L())
}
