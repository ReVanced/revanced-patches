package app.revanced.patches.music.utils.intenthook.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object FullStackTraceActivityFingerprint : MethodFingerprint(
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/FullStackTraceActivity;") && methodDef.name == "onCreate"
    }
)
