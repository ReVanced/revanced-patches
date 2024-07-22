package app.revanced.patches.all.privacy.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object AppsFlyerInitFingerprint : MethodFingerprint(
    returnType = "Ljava/lang/Object;",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.STATIC or AccessFlags.SYNTHETIC,
    parameters = listOf("L", "L", "L"),
    customFingerprint = { _, classDef ->
        classDef.sourceFile == "CatchingAppsFlyerLibWrapper.kt"
    }
)