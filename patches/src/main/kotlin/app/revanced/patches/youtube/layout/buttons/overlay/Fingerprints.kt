package app.revanced.patches.youtube.layout.buttons.overlay

import app.revanced.patcher.fingerprint
import app.revanced.util.containsLiteralInstruction
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags

internal val playerControlsPreviousNextOverlayTouchFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    strings("1.0x")
    custom { methodDef, _ ->
        methodDef.containsLiteralInstruction(playerControlPreviousButtonTouchArea) &&
            methodDef.containsLiteralInstruction(playerControlNextButtonTouchArea)
    }
}

internal val mediaRouteButtonFingerprint = fingerprint {
    parameters("I")
    custom { methodDef, _ ->
        methodDef.definingClass.endsWith("/MediaRouteButton;") && methodDef.name == "setVisibility"
    }
}

internal val inflateControlsGroupLayoutStubFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters()
    returns("V")
    literal { controlsButtonGroupLayoutStub }
}
