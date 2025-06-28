package app.revanced.patches.youtube.layout.buttons.overlay

import app.revanced.patcher.fingerprint
import app.revanced.patcher.methodCall
import app.revanced.patches.shared.misc.mapping.resourceLiteral
import com.android.tools.smali.dexlib2.AccessFlags

internal val mediaRouteButtonFingerprint by fingerprint {
    parameters("I")
    custom { methodDef, _ ->
        methodDef.definingClass.endsWith("/MediaRouteButton;") && methodDef.name == "setVisibility"
    }
}

internal val inflateControlsGroupLayoutStubFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters()
    returns("V")
    instructions(
        resourceLiteral("id", "youtube_controls_button_group_layout_stub"),
        methodCall(name = "inflate")
    )
}
