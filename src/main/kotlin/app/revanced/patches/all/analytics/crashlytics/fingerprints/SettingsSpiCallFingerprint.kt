package app.revanced.patches.all.analytics.crashlytics.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object SettingsSpiCallFingerprint : MethodFingerprint(
    returnType = "L",
    accessFlags = AccessFlags.PUBLIC.value,
    strings = listOf("Settings request failed."),
    customFingerprint = { _, classDef ->
        classDef.sourceFile == "DefaultSettingsSpiCall.java"
    }
)