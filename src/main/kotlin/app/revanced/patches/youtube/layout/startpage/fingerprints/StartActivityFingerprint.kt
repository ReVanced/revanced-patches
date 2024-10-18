package app.revanced.patches.youtube.layout.startpage.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

/**
 * Resolves using class found in [StartActivityParentFingerprint].
 */
object StartActivityFingerprint : MethodFingerprint(
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
    customFingerprint = { method, classDef ->
        method.name == "onCreate"
    }
)