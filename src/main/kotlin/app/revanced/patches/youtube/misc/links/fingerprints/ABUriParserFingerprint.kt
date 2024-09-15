package app.revanced.patches.youtube.misc.links.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patches.youtube.misc.links.BypassURLRedirectsPatch.findUriParseIndex
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Target 19.33+
 */
internal object ABUriParserFingerprint : MethodFingerprint(
    returnType = "Ljava/lang/Object",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("Ljava/lang/Object"),
    strings = listOf(
        "Found entityKey=`",
        "` that does not contain a PlaylistVideoEntityId message as it's identifier."
    ),
    customFingerprint = { methodDef, _ ->
        methodDef.findUriParseIndex() >= 0
    }
)
