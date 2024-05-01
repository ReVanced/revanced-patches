package app.revanced.patches.youtube.misc.integrations.fingerprints

import app.revanced.patches.shared.misc.integrations.integrationsHook
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * For embedded playback inside 3rd party android app (such as 3rd party Reddit apps).
 */
internal val remoteEmbeddedPlayerFingerprint = integrationsHook(
    // Integrations context is the first method parameter.
    contextRegisterResolver = { it.implementation!!.registerCount - it.parameters.size },
) {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.CONSTRUCTOR)
    returns("V")
    parameters("Landroid/content/Context;", "L", "L", "Z")
    custom { methodDef, _ ->
        methodDef.definingClass == "Lcom/google/android/youtube/api/jar/client/RemoteEmbeddedPlayer;"
    }
}
