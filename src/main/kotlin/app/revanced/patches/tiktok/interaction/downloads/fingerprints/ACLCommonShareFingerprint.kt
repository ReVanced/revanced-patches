package app.revanced.patches.tiktok.interaction.downloads.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object ACLCommonShareFingerprint : MethodFingerprint(
    "I",
    AccessFlags.PUBLIC or AccessFlags.FINAL,
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/ACLCommonShare;") &&
                methodDef.name == "getCode"
    }
)