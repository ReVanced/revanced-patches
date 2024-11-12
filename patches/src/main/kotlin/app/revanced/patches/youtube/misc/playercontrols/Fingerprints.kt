package app.revanced.patches.youtube.misc.playercontrols

import app.revanced.patcher.fingerprint
import app.revanced.util.containsLiteralInstruction
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags

internal val playerTopControlsInflateFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters()
    literal { controlsLayoutStub }
}

internal val playerControlsExtensionHookFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("V")
    parameters("Z")
    custom { methodDef, classDef ->
        methodDef.name == "fullscreenButtonVisibilityChanged" &&
            classDef.type == "Lapp/revanced/extension/youtube/patches/PlayerControlsPatch;"
    }
}

internal val playerBottomControlsInflateFingerprint = fingerprint {
    returns("Ljava/lang/Object;")
    parameters()
    literal { bottomUiContainerResourceId }
}

internal val overlayViewInflateFingerprint = fingerprint {
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

