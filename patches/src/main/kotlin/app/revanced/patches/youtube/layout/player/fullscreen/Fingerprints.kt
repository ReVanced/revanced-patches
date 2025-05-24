package app.revanced.patches.youtube.layout.player.fullscreen

import app.revanced.patcher.fingerprint
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags

internal const val OPEN_VIDEOS_FULLSCREEN_PORTRAIT_FEATURE_FLAG = 45666112L

internal val openVideosFullscreenPortraitFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L", "Lj\$/util/Optional;")
    literal {
        OPEN_VIDEOS_FULLSCREEN_PORTRAIT_FEATURE_FLAG
    }
}

internal val openVideosFullscreenHookPatchExtensionFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returns("Z")
    parameters()
    custom { methodDef, classDef ->
        methodDef.name == "isFullScreenPatchIncluded" && classDef.type == EXTENSION_CLASS_DESCRIPTOR
    }
}
