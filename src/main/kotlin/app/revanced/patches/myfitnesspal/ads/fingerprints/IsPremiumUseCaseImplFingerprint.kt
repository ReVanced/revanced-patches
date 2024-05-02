package app.revanced.patches.myfitnesspal.ads.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object IsPremiumUseCaseImplFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC.value,
    customFingerprint = { methodDef, classDef ->
        classDef.type.endsWith("IsPremiumUseCaseImpl;") && methodDef.name == "doWork"
    }
)
