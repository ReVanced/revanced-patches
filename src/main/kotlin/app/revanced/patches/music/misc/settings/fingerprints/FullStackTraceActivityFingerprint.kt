package app.revanced.patches.music.misc.settings.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object FullStackTraceActivityFingerprint : MethodFingerprint(
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/FullStackTraceActivity;") && methodDef.name == "onCreate"
    }
)
