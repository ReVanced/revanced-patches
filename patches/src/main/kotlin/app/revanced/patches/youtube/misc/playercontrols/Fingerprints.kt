package app.revanced.patches.youtube.misc.playercontrols

import app.revanced.patcher.fingerprint
import app.revanced.util.containsLiteralInstruction
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import app.revanced.util.indexOfFirstInstructionReversed
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal fun indexOfFocusableInTouchModeInstruction(method: Method) =
    method.indexOfFirstInstruction {
        getReference<MethodReference>()?.name == "setFocusableInTouchMode"
    }

internal fun indexOfTranslationInstruction(method: Method) =
    method.indexOfFirstInstructionReversed {
        getReference<MethodReference>()?.name == "setTranslationY"
    }

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
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returns("V")
    parameters()
    custom { method, _ ->
        indexOfFocusableInTouchModeInstruction(method) >= 0 &&
        method.containsLiteralInstruction(inset_overlay_view_layout_id) &&
                method.containsLiteralInstruction(scrim_overlay_id)

    }
}

internal val motionEventFingerprint = fingerprint {
    returns("V")
    parameters("Landroid/view/MotionEvent;")
    custom { method, _ ->
        indexOfTranslationInstruction(method) >= 0
    }
}

internal val playerTopControlsInflateFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters()
    literal { controls_layout_stub_id }
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

internal val playerBottomControlsInflateFingerprint = fingerprint {
    returns("Ljava/lang/Object;")
    parameters()
    literal { bottom_ui_container_stub_id }
}

internal val overlayViewInflateFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Landroid/view/View;")
    custom { methodDef, _ ->
        methodDef.containsLiteralInstruction(fullscreen_button_id) &&
            methodDef.containsLiteralInstruction(heatseeker_viewstub_id)
    }
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
    literal { 45643739L }
}

internal val playerTopControlsExperimentalLayoutFeatureFlagFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("I")
    parameters()
    literal { 45629424L }
}

