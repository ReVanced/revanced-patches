package app.revanced.patches.twitch.chat.antidelete.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags

object DeletedMessageClickableSpanCtorFingerprint : MethodFingerprint(
    "V", AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("DeletedMessageClickableSpan;")
    }
)