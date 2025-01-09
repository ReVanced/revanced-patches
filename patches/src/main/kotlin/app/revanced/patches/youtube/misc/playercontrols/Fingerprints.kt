package app.revanced.patches.youtube.misc.playercontrols

import app.revanced.patcher.fingerprint
import app.revanced.patcher.literal
import app.revanced.patches.shared.misc.mapping.resourceLiteral
import app.revanced.util.containsLiteralInstruction
import com.android.tools.smali.dexlib2.AccessFlags

internal val playerTopControlsInflateFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters()
    instructions(
        resourceLiteral("id", "controls_layout_stub")
    )
}

internal val playerControlsExtensionHookListenersExistFingerprint by fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returns("Z")
    parameters()
    custom { methodDef, classDef ->
        methodDef.name == "fullscreenButtonVisibilityCallbacksExist" &&
                classDef.type == EXTENSION_CLASS_DESCRIPTOR
    }
}

internal val playerControlsExtensionHookFingerprint by fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returns("V")
    parameters("Z")
    custom { methodDef, classDef ->
        methodDef.name == "fullscreenButtonVisibilityChanged" &&
            classDef.type == EXTENSION_CLASS_DESCRIPTOR
    }
}

internal val playerBottomControlsInflateFingerprint by fingerprint {
    returns("Ljava/lang/Object;")
    parameters()
    instructions(
        resourceLiteral("id", "bottom_ui_container_stub")
    )
}

internal val overlayViewInflateFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Landroid/view/View;")
    custom { methodDef, _ ->
        methodDef.containsLiteralInstruction(fullscreenButton) &&
            methodDef.containsLiteralInstruction(heatseekerViewstub)
    }
}

/**
 * Resolves to the class found in [playerTopControlsInflateFingerprint].
 */
internal val controlsOverlayVisibilityFingerprint by fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returns("V")
    parameters("Z", "Z")
}

internal val playerBottomControlsExploderFeatureFlagFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters()
    instructions(
        literal(45643739L)
    )
}

internal val playerTopControlsExperimentalLayoutFeatureFlagFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("I")
    parameters()
    instructions(
        literal(45629424L)
    )
}

