package app.revanced.patches.youtube.misc.extension.hooks

import app.revanced.patches.shared.misc.extension.extensionHook
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Old API activity to embed YouTube into 3rd party Android apps.
 *
 * In 2023 supported was ended and is no longer available,
 * but this may still be used by older apps:
 * https://developers.google.com/youtube/android/player
 */
// Extension context is the Activity itself.
internal val standalonePlayerActivityHook = extensionHook(
    contextRegisterResolver = { it.implementation!!.registerCount - it.parameters.size },
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L")
    custom { method, classDef ->
        classDef.type == "Lcom/google/android/youtube/api/StandalonePlayerActivity;" &&
            method.name == "onCreate"
    }
}
