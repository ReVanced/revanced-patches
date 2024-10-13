package app.revanced.patches.youtube.misc.extension.hooks

import app.revanced.patches.shared.misc.extension.extensionHook
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * For embedded playback.
 * It appears this hook may no longer be needed as one of the constructor parameters is the already hooked
 * [embeddedPlayerControlsOverlayHook]
 */
internal val apiPlayerServiceHook = extensionHook(
    // Extension context is the first method parameter.
    contextRegisterResolver = { it.implementation!!.registerCount - it.parameters.size },
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    custom { _, classDef ->
        classDef.type == "Lcom/google/android/apps/youtube/embeddedplayer/service/service/jar/ApiPlayerService;"
    }
}
