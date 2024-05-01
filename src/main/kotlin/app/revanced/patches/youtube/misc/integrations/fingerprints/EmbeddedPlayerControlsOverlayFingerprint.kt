package app.revanced.patches.youtube.misc.integrations.fingerprints

import app.revanced.patches.shared.misc.integrations.integrationsHook
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * For embedded playback inside Google Play Store (and probably other situations as well).
 *
 * Note: this fingerprint may no longer be needed, as it appears
 * [remoteEmbedFragmentFingerprint] may be set before this hook is called.
 */
internal val embeddedPlayerControlsOverlayFingerprint = integrationsHook(
    // Integrations context is the first method parameter.
    contextRegisterResolver = { it.implementation!!.registerCount - it.parameters.size },
) {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.CONSTRUCTOR)
    returns("V")
    parameters("Landroid/content/Context;", "L", "L")
    custom { methodDef, _ ->
        methodDef.definingClass.startsWith("Lcom/google/android/apps/youtube/embeddedplayer/service/ui/overlays/controlsoverlay/remoteloaded/")
    }
}
