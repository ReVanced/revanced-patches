package app.revanced.patches.youtube.misc.integrations.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.shared.misc.integrations.BaseIntegrationsPatch.IntegrationsFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * For embedded playback.
 * It appears this hook may no longer be needed as one of the constructor parameters is the already hooked
 * [EmbeddedPlayerControlsOverlayFingerprint]
 */
internal object APIPlayerServiceFingerprint : IntegrationsFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    customFingerprint = { methodDef, _ -> methodDef.definingClass == "Lcom/google/android/apps/youtube/embeddedplayer/service/service/jar/ApiPlayerService;" },
    // Integrations context is the first method parameter.
    contextRegisterResolver = { it.implementation!!.registerCount - it.parameters.size }
)