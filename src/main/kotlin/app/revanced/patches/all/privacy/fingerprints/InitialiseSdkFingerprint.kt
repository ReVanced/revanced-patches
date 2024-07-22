package app.revanced.patches.all.privacy.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object InitialiseSdkFingerprint : MethodFingerprint(
    returnType = "L",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    strings = listOf("moEngage"),
    customFingerprint = { methodDef, classDef ->
        classDef.sourceFile == "InitialisationHandler.kt" && methodDef.name == "initialiseSdk"
    }
)