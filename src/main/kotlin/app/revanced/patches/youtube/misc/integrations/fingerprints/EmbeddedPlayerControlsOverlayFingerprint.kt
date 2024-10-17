package app.revanced.patches.youtube.misc.integrations.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.shared.misc.integrations.BaseIntegrationsPatch.IntegrationsFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * For embedded playback inside Google Play store (and probably other situations as well).
 *
 * Note: this fingerprint may no longer be needed, as it appears
 * [RemoteEmbedFragmentFingerprint] may be set before this hook is called.
 */
@Deprecated("Code was removed in target 19.39+")
internal object EmbeddedPlayerControlsOverlayFingerprint : IntegrationsFingerprint(
    accessFlags = AccessFlags.PRIVATE or AccessFlags.CONSTRUCTOR,
    returnType = "V",
    parameters = listOf("Landroid/content/Context;", "L", "L"),
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.startsWith("Lcom/google/android/apps/youtube/embeddedplayer/service/ui/overlays/controlsoverlay/remoteloaded/")
    },
    contextRegisterResolver = { "p1" },
    isOptional = true
)