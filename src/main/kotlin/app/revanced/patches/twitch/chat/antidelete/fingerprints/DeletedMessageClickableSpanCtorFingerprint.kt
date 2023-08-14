package app.revanced.patches.twitch.chat.antidelete.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object DeletedMessageClickableSpanCtorFingerprint : MethodFingerprint(
    "V", AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("DeletedMessageClickableSpan;")
    }
)