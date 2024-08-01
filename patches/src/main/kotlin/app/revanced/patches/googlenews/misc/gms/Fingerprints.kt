package app.revanced.patches.googlenews.misc.gms

import app.revanced.patcher.fingerprint

internal val magazinesActivityOnCreateFingerprint = fingerprint {
    custom { methodDef, classDef ->
        methodDef.name == "onCreate" && classDef.endsWith("/StartActivity;")
    }
}
