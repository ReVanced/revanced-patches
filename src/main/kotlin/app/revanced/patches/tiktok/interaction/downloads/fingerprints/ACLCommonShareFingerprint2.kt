package app.revanced.patches.tiktok.interaction.downloads.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.tiktok.interaction.downloads.annotations.DownloadsCompatibility
import com.android.tools.smali.dexlib2.AccessFlags

@Name("Acl common share get show type")
@DownloadsCompatibility
object ACLCommonShareFingerprint2 : MethodFingerprint(
    "I",
    AccessFlags.PUBLIC or AccessFlags.FINAL,
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/ACLCommonShare;") &&
                methodDef.name == "getShowType"
    }
)