package app.revanced.patches.music.misc.gms

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val googlePlayUtilityFingerprint = fingerprint {
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

internal val musicActivityOnCreateFingerprint = fingerprint {
    returns("V")
    parameters("Landroid/os/Bundle;")
    custom { method, classDef ->
        method.name == "onCreate" && classDef.endsWith("/MusicActivity;")
    }
}

internal val serviceCheckFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("V")
    strings("Google Play Services not available")
}
