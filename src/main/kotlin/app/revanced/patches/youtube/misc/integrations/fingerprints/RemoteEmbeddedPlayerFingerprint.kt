package app.revanced.patches.youtube.misc.integrations.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.shared.misc.integrations.BaseIntegrationsPatch.IntegrationsFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * For embedded playback inside 3rd party android app (such as 3rd party Reddit apps).
 */
@Deprecated("Code was removed in target 19.39+")
internal object RemoteEmbeddedPlayerFingerprint : IntegrationsFingerprint(
    accessFlags = AccessFlags.PRIVATE or AccessFlags.CONSTRUCTOR,
    returnType = "V",
    parameters = listOf("Landroid/content/Context;", "L", "L", "Z"),
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass == "Lcom/google/android/youtube/api/jar/client/RemoteEmbeddedPlayer;"
    },
    contextRegisterResolver = { "p1" },
    isOptional = true
)