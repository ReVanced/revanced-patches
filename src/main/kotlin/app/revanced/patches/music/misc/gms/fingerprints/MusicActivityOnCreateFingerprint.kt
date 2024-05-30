package app.revanced.patches.music.misc.gms.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val musicActivityOnCreateFingerprint = methodFingerprint {
    returns("V")
    parameters("Landroid/os/Bundle;")
    custom { methodDef, classDef ->
        methodDef.name == "onCreate" && classDef.endsWith("/MusicActivity;")
    }
}
