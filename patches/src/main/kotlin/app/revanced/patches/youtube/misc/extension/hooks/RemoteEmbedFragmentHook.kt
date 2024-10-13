package app.revanced.patches.youtube.misc.extension.hooks

import app.revanced.patches.shared.misc.extension.extensionHook
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * For embedded playback. Likely covers Google Play Store and other Google products.
 */
internal val remoteEmbedFragmentHook = extensionHook(
    // Extension context is the first method parameter.
    contextRegisterResolver = { it.implementation!!.registerCount - it.parameters.size },
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returns("V")
    parameters("Landroid/content/Context;", "L", "L")
    custom { _, classDef ->
        classDef.type == "Lcom/google/android/apps/youtube/embeddedplayer/service/jar/client/RemoteEmbedFragment;"
    }
}
