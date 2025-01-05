package app.revanced.patches.youtube.layout.buttons.overlay

import app.revanced.patcher.MethodCallFilter
import app.revanced.patcher.fingerprint
import app.revanced.patches.shared.misc.mapping.ResourceMappingFilter
import com.android.tools.smali.dexlib2.AccessFlags

internal val playerControlsPreviousNextOverlayTouchFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    strings("1.0x")
    instructions(
        ResourceMappingFilter("id", "player_control_previous_button_touch_area"),
        ResourceMappingFilter("id", "player_control_next_button_touch_area"),
        MethodCallFilter(parameters = listOf("Landroid/view/View;", "I"))
    )
}

internal val mediaRouteButtonFingerprint by fingerprint {
    parameters("I")
    custom { methodDef, _ ->
        methodDef.definingClass.endsWith("/MediaRouteButton;") && methodDef.name == "setVisibility"
    }
}
