package app.revanced.patches.youtube.misc.integrations.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.shared.misc.integrations.BaseIntegrationsPatch.IntegrationsFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * For embedded playback inside the Google app (such as the in app 'discover' tab).
 *
 * Note: this fingerprint may or may not be needed, as
 * [RemoteEmbedFragmentFingerprint] might be set before this is called.
 */
internal object EmbeddedPlayerFingerprint : IntegrationsFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.STATIC,
    returnType = "L",
    parameters = listOf("L", "L", "Landroid/content/Context;"),
    strings = listOf("android.hardware.type.television"), // String is also found in other classes
    // Integrations context is the third method parameter.
    contextRegisterResolver = { it.implementation!!.registerCount - it.parameters.size + 2 }
)