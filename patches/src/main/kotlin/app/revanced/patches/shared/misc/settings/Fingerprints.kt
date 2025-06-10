package app.revanced.patches.shared.misc.settings

import app.revanced.patcher.fingerprint
import app.revanced.patches.shared.misc.extension.EXTENSION_CLASS_DESCRIPTOR
import com.android.tools.smali.dexlib2.AccessFlags

internal val themeDarkColorFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returns("Ljava/lang/String;")
    parameters()
    custom { method, _ ->
        method.name == "darkThemeResourceName" && method.definingClass == EXTENSION_CLASS_DESCRIPTOR
    }
}

internal val themeLightColorFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returns("Ljava/lang/String;")
    parameters()
    custom { method, _ ->
        method.name == "lightThemeResourceName" && method.definingClass == EXTENSION_CLASS_DESCRIPTOR
    }
}
