package app.revanced.patches.youtube.misc.extension.hooks

import app.revanced.patches.shared.misc.extension.extensionHook
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * For embedded playback inside 3rd party android app (such as 3rd party Reddit apps).
 */
internal val remoteEmbeddedPlayerHook = extensionHook(
    // Extension context is the first method parameter.
    contextRegisterResolver = { it.implementation!!.registerCount - it.parameters.size },
) {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.CONSTRUCTOR)
    returns("V")
    parameters("Landroid/content/Context;", "L", "L", "Z")
    custom { _, classDef ->
        classDef.type == "Lcom/google/android/youtube/api/jar/client/RemoteEmbeddedPlayer;"
    }
}
