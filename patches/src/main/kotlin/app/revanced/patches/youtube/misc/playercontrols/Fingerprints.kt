package app.revanced.patches.youtube.misc.playercontrols

import app.revanced.patcher.InstructionLocation.MatchAfterImmediately
import app.revanced.patcher.checkCast
import app.revanced.patcher.fingerprint
import app.revanced.patcher.literal
import app.revanced.patcher.methodCall
import app.revanced.patcher.opcode
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.shared.misc.mapping.resourceLiteral
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val playerControlsVisibilityEntityModelFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC)
    returns("L")
    parameters()
    opcodes(
        Opcode.IGET,
        Opcode.INVOKE_STATIC
    )
    custom { method, _ ->
        method.name == "getPlayerControlsVisibility"
    }
}

internal val youtubeControlsOverlayFingerprint = fingerprint {
    returns("V")
    parameters()
    instructions(
        methodCall(name = "setFocusableInTouchMode"),
        resourceLiteral(ResourceType.ID, "inset_overlay_view_layout"),
        resourceLiteral(ResourceType.ID, "scrim_overlay"),
    )
}

internal val motionEventFingerprint = fingerprint {
    returns("V")
    parameters("Landroid/view/MotionEvent;")
    instructions(
        methodCall(name = "setTranslationY")
    )
}

internal val playerControlsExtensionHookListenersExistFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returns("Z")
    parameters()
    custom { methodDef, classDef ->
        methodDef.name == "fullscreenButtonVisibilityCallbacksExist" &&
                classDef.type == EXTENSION_CLASS_DESCRIPTOR
    }
}

internal val playerControlsExtensionHookFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returns("V")
    parameters("Z")
    custom { methodDef, classDef ->
        methodDef.name == "fullscreenButtonVisibilityChanged" &&
            classDef.type == EXTENSION_CLASS_DESCRIPTOR
    }
}

internal val playerTopControlsInflateFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters()
    instructions(
        resourceLiteral(ResourceType.ID, "controls_layout_stub"),
        methodCall("Landroid/view/ViewStub;", "inflate"),
        opcode(Opcode.MOVE_RESULT_OBJECT, MatchAfterImmediately())
    )
}

internal val playerBottomControlsInflateFingerprint = fingerprint {
    returns("Ljava/lang/Object;")
    parameters()
    instructions(
        resourceLiteral(ResourceType.ID, "bottom_ui_container_stub"),
        methodCall("Landroid/view/ViewStub;", "inflate"),
        opcode(Opcode.MOVE_RESULT_OBJECT, MatchAfterImmediately())
    )
}

internal val overlayViewInflateFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Landroid/view/View;")
    instructions(
        resourceLiteral(ResourceType.ID, "heatseeker_viewstub"),
        resourceLiteral(ResourceType.ID, "fullscreen_button"),
        checkCast("Landroid/widget/ImageView;")
    )
}

/**
 * Resolves to the class found in [playerTopControlsInflateFingerprint].
 */
internal val controlsOverlayVisibilityFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returns("V")
    parameters("Z", "Z")
}

internal val playerBottomControlsExploderFeatureFlagFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters()
    instructions(
        literal(45643739L)
    )
}

internal val playerTopControlsExperimentalLayoutFeatureFlagFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("I")
    parameters()
    instructions(
        literal(45629424L)
    )
}

internal val playerControlsLargeOverlayButtonsFeatureFlagFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters()
    instructions(
        literal(45709810L)
    )
}

internal val playerControlsFullscreenLargeButtonsFeatureFlagFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters()
    instructions(
        literal(45686474L)
    )
}

internal val playerControlsButtonStrokeFeatureFlagFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters()
    instructions(
        literal(45713296)
    )
}