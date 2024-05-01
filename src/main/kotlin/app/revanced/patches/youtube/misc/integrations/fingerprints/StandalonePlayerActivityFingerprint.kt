package app.revanced.patches.youtube.misc.integrations.fingerprints

import app.revanced.patches.shared.misc.integrations.integrationsHook
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Old API activity to embed YouTube into 3rd party Android apps.
 *
 * In 2023 supported was ended and is no longer available,
 * but this may still be used by older apps:
 * https://developers.google.com/youtube/android/player
 */
// Integrations context is the Activity itself.
internal val standalonePlayerActivityFingerprint = integrationsHook(
    contextRegisterResolver = { it.implementation!!.registerCount - it.parameters.size },
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L")
    custom { methodDef, _ ->
        methodDef.definingClass == "Lcom/google/android/youtube/api/StandalonePlayerActivity;" &&
            methodDef.name == "onCreate"
    }
}
