package app.revanced.patches.tiktok.interaction.downloads

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val aclCommonShareFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("I")
    custom { method, classDef ->
        classDef.endsWith("/ACLCommonShare;") &&
                method.name == "getCode"
    }
}

internal val aclCommonShare2Fingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("I")
    custom { method, classDef ->
        classDef.endsWith("/ACLCommonShare;") &&
                method.name == "getShowType"
    }
}

internal val aclCommonShare3Fingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("I")
    custom { method, classDef ->
        classDef.endsWith("/ACLCommonShare;") &&
                method.name == "getTranscode"
    }
}

internal val downloadUriFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("Landroid/net/Uri;")
    parameters(
        "Landroid/content/Context;",
        "Ljava/lang/String;"
    )
    strings(
        "/",
        "/Camera",
        "/Camera/",
        "video/mp4"
    )
}
