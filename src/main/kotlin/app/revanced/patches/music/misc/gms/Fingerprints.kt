package app.revanced.patches.music.misc.gms

import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint.methodFingerprint

internal val googlePlayUtilityFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("I")
    strings(
        "This should never happen.",
        "MetadataValueReader",
        "GooglePlayServicesUtil",
        "com.android.vending",
        "android.hardware.type.embedded",
    )
}

internal val musicActivityOnCreateFingerprint = methodFingerprint {
    returns("V")
    parameters("Landroid/os/Bundle;")
    custom { methodDef, classDef ->
        methodDef.name == "onCreate" && classDef.endsWith("/MusicActivity;")
    }
}

internal val serviceCheckFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("V")
    strings("Google Play Services not available")
}
