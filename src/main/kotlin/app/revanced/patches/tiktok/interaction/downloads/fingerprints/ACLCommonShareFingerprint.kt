package app.revanced.patches.tiktok.interaction.downloads.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val aclCommonShareFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("I")
    custom { methodDef, classDef ->
        classDef.endsWith("/ACLCommonShare;") &&
            methodDef.name == "getCode"
    }
}
