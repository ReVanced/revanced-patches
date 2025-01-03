package app.revanced.patches.youtube.layout.player.fullscreen

import app.revanced.patcher.LiteralFilter
import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val openVideosFullscreenPortraitFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L", "Lj\$/util/Optional;")
    instructions(
        LiteralFilter(45666112L)
    )
}

/**
 * Used to enable opening regular videos fullscreen.
 */
internal val openVideosFullscreenHookPatchExtensionFingerprint by fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returns("Z")
    parameters()
    custom { methodDef, classDef ->
        methodDef.name == "isFullScreenPatchIncluded" && classDef.type == EXTENSION_CLASS_DESCRIPTOR
    }
}
