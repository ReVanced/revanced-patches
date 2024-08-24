package app.revanced.patches.all.privacy.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object InitAnalyticsFingerprint : MethodFingerprint(
    returnType = "L",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.STATIC,
    parameters = listOf("Landroid/content/Context;"),
    strings = listOf("Slow initialization (ms)"),
    customFingerprint = { _, classDef ->
        classDef.sourceFile?.startsWith("com.google.android.gms:play-services-analytics-impl") == true
    },
)
