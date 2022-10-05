package app.revanced.patches.tiktok.interaction.downloads.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.tiktok.interaction.downloads.annotations.DownloadsCompatibility
import org.jf.dexlib2.AccessFlags

@Name("acl-common-share-get-show-type")
@DownloadsCompatibility
@Version("0.0.1")
object ACLCommonShareFingerprint2 : MethodFingerprint(
    "I",
    AccessFlags.PUBLIC or AccessFlags.FINAL,
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("/ACLCommonShare;") &&
                methodDef.name == "getShowType"
    }
)