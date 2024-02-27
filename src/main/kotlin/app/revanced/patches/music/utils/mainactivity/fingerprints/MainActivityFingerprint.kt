package app.revanced.patches.music.utils.mainactivity.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object MainActivityFingerprint : MethodFingerprint(
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
    strings = listOf(
        "android.intent.action.MAIN",
        "FEmusic_home"
    ),
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("Activity;")
                && methodDef.name == "onCreate"
    }
)