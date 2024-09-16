package app.revanced.patches.youtube.layout.startpage.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object StartActivityLegacyFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    returnType = "V",
    parameters = listOf("Landroid/content/Intent;"),
    customFingerprint = { method, classDef ->
        method.name == "startActivity" && classDef.type.endsWith("/Shell_HomeActivity;")
    }
)