package app.revanced.patches.tiktok.interaction.downloads.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object ACLCommonShareFingerprint2 : MethodFingerprint(
    "I",
    AccessFlags.PUBLIC or AccessFlags.FINAL,
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/ACLCommonShare;") &&
                methodDef.name == "getShowType"
    }
)