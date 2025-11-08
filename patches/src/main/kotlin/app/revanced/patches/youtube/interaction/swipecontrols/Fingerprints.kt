package app.revanced.patches.youtube.interaction.swipecontrols

import app.revanced.patcher.fingerprint
import app.revanced.patcher.literal
import com.android.tools.smali.dexlib2.AccessFlags

internal val swipeControlsHostActivityFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters()
    custom { method, _ ->
        method.definingClass == EXTENSION_CLASS_DESCRIPTOR
    }
}

internal val swipeChangeVideoFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    instructions(
        literal(45631116L) // Swipe to change fullscreen video feature flag.
    )
}
