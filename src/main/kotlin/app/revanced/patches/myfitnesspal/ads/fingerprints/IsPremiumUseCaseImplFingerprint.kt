package app.revanced.patches.myfitnesspal.ads.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

val isPremiumUseCaseImplFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC)
    custom { methodDef, classDef ->
        classDef.endsWith("IsPremiumUseCaseImpl;") && methodDef.name == "doWork"
    }
}