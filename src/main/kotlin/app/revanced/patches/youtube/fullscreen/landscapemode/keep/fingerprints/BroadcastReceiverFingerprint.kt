package app.revanced.patches.youtube.fullscreen.landscapemode.keep.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object BroadcastReceiverFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("Landroid/content/Context;", "Landroid/content/Intent;"),
    strings = listOf(
        "android.intent.action.SCREEN_ON",
        "android.intent.action.SCREEN_OFF",
        "android.intent.action.BATTERY_CHANGED"
    ),
    customFingerprint = { _, classDef ->
        classDef.superclass == "Landroid/content/BroadcastReceiver;"
    }
)