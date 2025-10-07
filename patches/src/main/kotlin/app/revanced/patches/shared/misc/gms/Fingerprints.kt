package app.revanced.patches.shared.misc.gms

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val googlePlayUtilityFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("I")
    parameters("L", "I")
    strings(
        "This should never happen.",
        "MetadataValueReader",
        "com.google.android.gms",
    )
}

internal val serviceCheckFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("V")
    parameters("L", "I")
    strings("Google Play Services not available")
}

internal val gmsCoreSupportFingerprint by fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returns("Ljava/lang/String;")
    parameters()
    custom { method, classDef ->
        method.name == "getGmsCoreVendorGroupId" && classDef.type == EXTENSION_CLASS_DESCRIPTOR
    }
}

internal val originalPackageNameExtensionFingerprint by fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returns("Ljava/lang/String;")
    parameters()
    custom { methodDef, classDef ->
        methodDef.name == "getOriginalPackageName" && classDef.type == EXTENSION_CLASS_DESCRIPTOR
    }
}
