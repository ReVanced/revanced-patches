package app.revanced.patches.googlephotos.misc.gms

import app.revanced.patcher.fingerprint

internal val homeActivityOnCreateFingerprint by fingerprint {
    custom { methodDef, classDef ->
        methodDef.name == "onCreate" && classDef.endsWith("/HomeActivity;")
    }
}
