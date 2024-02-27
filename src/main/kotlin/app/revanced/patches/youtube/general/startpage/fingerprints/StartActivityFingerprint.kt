package app.revanced.patches.youtube.general.startpage.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object StartActivityFingerprint : MethodFingerprint(
    parameters = listOf("Landroid/content/Intent;"),
    customFingerprint = { methodDef, classDef ->
        methodDef.name == "startActivity"
                && classDef.type.endsWith("/Shell_HomeActivity;")
    }
)