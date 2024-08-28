package app.revanced.patches.youtube.misc.extensions.hooks

import app.revanced.patches.shared.misc.extensions.extensionsHook
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * For embedded playback inside Google Play Store (and probably other situations as well).
 *
 * Note: This hook may no longer be needed, as it appears
 * [remoteEmbedFragmentHook] may be set before this hook is called.
 */
internal val embeddedPlayerControlsOverlayHook = extensionsHook(
    // Extension context is the first method parameter.
    contextRegisterResolver = { it.implementation!!.registerCount - it.parameters.size },
) {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.CONSTRUCTOR)
    returns("V")
    parameters("Landroid/content/Context;", "L", "L")
    custom { _, classDef ->
        classDef.startsWith("Lcom/google/android/apps/youtube/embeddedplayer/service/ui/overlays/controlsoverlay/remoteloaded/")
    }
}
