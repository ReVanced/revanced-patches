package app.revanced.patches.music.misc.gms.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object MusicActivityOnCreateFingerprint : MethodFingerprint(
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
    customFingerprint = { methodDef, classDef ->
        methodDef.name == "onCreate" && classDef.type.endsWith("/MusicActivity;")
    }
)