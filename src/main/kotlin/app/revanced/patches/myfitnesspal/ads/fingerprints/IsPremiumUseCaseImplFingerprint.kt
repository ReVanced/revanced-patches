package app.revanced.patches.myfitnesspal.ads.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

val isPremiumUseCaseImplFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC.value)
    custom { methodDef, classDef ->
        classDef.type.endsWith("IsPremiumUseCaseImpl;") && methodDef.name == "doWork"
    }
}
