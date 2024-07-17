package app.revanced.patches.all.analytics.statsig.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object StatsigClientFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    customFingerprint = { methodDef, classDef ->
        classDef.sourceFile == "StatsigClient.kt" && methodDef.name == "initializeAsync"
    }
)