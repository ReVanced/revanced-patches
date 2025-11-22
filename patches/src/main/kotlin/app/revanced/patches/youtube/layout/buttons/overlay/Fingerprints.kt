package app.revanced.patches.youtube.layout.buttons.overlay

import app.revanced.patcher.fingerprint
import app.revanced.patcher.literal
import app.revanced.patcher.methodCall
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.shared.misc.mapping.resourceLiteral
import com.android.tools.smali.dexlib2.AccessFlags

internal val mediaRouteButtonFingerprint = fingerprint {
    parameters("I")
    custom { methodDef, _ ->
        methodDef.definingClass.endsWith("/MediaRouteButton;") && methodDef.name == "setVisibility"
    }
}

internal val castButtonPlayerFeatureFlagFingerprint = fingerprint {
    returns("Z")
    instructions(
        literal(45690091)
    )
}

internal val castButtonActionFeatureFlagFingerprint = fingerprint {
    returns("Z")
    instructions(
        literal(45690090)
    )
}

internal val inflateControlsGroupLayoutStubFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters()
    returns("V")
    instructions(
        resourceLiteral(ResourceType.ID, "youtube_controls_button_group_layout_stub"),
        methodCall(name = "inflate")
    )
}
