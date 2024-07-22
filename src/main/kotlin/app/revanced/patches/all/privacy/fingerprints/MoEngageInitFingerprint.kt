package app.revanced.patches.all.privacy.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object MoEngageInitFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC.value,
    customFingerprint = { methodDef, classDef ->
        classDef.sourceFile == "DefaultMoEngageSdk.kt" && methodDef.name == "init"
    }
)