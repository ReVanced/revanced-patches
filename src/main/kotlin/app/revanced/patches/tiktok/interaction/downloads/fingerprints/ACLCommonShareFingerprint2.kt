package app.revanced.patches.tiktok.interaction.downloads.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val aclCommonShareFingerprint2 = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("I")
    custom { methodDef, _ ->
        methodDef.definingClass.endsWith("/ACLCommonShare;") &&
            methodDef.name == "getShowType"
    }
}
