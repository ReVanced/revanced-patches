package app.revanced.patches.youtube.layout.buttons.overlay

import app.revanced.patcher.fingerprint
import app.revanced.patcher.methodCall
import app.revanced.patches.shared.misc.mapping.resourceLiteral
import com.android.tools.smali.dexlib2.AccessFlags

internal val playerControlsPreviousNextOverlayTouchFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    instructions(
        resourceLiteral("id", "player_control_previous_button_touch_area"),
        resourceLiteral("id", "player_control_next_button_touch_area"),
        methodCall(parameters = listOf("Landroid/view/View;", "I"))
    )
    strings("1.0x")
}

internal val mediaRouteButtonFingerprint by fingerprint {
    parameters("I")
    custom { methodDef, _ ->
        methodDef.definingClass.endsWith("/MediaRouteButton;") && methodDef.name == "setVisibility"
    }
}
