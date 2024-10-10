package app.revanced.patches.youtube.layout.buttons.overlay

import app.revanced.patcher.fingerprint
import app.revanced.util.containsWideLiteralInstructionValue
import com.android.tools.smali.dexlib2.AccessFlags

internal val playerControlsPreviousNextOverlayTouchFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    strings("1.0x")
    custom { methodDef, _ ->
        methodDef.containsWideLiteralInstructionValue(playerControlPreviousButtonTouchArea) &&
            methodDef.containsWideLiteralInstructionValue(playerControlNextButtonTouchArea)
    }
}

internal val mediaRouteButtonFingerprint = fingerprint {
    parameters("I")
    custom { methodDef, _ ->
        methodDef.definingClass.endsWith("/MediaRouteButton;") && methodDef.name == "setVisibility"
    }
}
