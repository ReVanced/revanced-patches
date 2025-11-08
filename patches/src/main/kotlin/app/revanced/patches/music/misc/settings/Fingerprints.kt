package app.revanced.patches.music.misc.settings

import app.revanced.patcher.fingerprint

internal val googleApiActivityFingerprint = fingerprint {
    returns("V")
    parameters("Landroid/os/Bundle;")
    custom { method, classDef ->
        classDef.endsWith("GoogleApiActivity;") && method.name == "onCreate"
    }
}
