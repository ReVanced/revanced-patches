package app.revanced.patches.youtube.misc.links.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patches.youtube.misc.links.BypassURLRedirectsPatch.findUriParseIndex
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Target 19.33+
 */
internal object HTTPUriParserFingerprint : MethodFingerprint(
    returnType = "Landroid/net/Uri",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.STATIC,
    parameters = listOf("Ljava/lang/String"),
    strings = listOf("https", "https:", "://"),
    customFingerprint = { methodDef, _ ->
        methodDef.findUriParseIndex() >= 0
    }
)