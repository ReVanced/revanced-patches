package app.revanced.patches.instagram.misc.share.domain

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val getCustomShareDomainFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returns("Ljava/lang/String;")
    parameters()
    custom { method, classDef ->
        method.name == "getCustomShareDomain" && classDef.type == EXTENSION_CLASS_DESCRIPTOR
    }
}
