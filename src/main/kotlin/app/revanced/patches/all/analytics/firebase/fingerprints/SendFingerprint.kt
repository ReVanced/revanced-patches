package app.revanced.patches.all.analytics.firebase.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object SendFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC.value,
    customFingerprint = { methodDef, classDef ->
        classDef.sourceFile == "TransportRuntime.java" && methodDef.name == "send"
    }
)