package app.revanced.patches.music.misc.gms

import app.revanced.patcher.fingerprint

internal val musicActivityOnCreateFingerprint = fingerprint {
    returns("V")
    parameters("Landroid/os/Bundle;")
    custom { method, classDef ->
        method.name == "onCreate" && classDef.endsWith("/MusicActivity;")
    }
}
