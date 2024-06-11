package app.revanced.patches.myfitnesspal.ads

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

val isPremiumUseCaseImplFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC)
    custom { methodDef, classDef ->
        classDef.endsWith("IsPremiumUseCaseImpl;") && methodDef.name == "doWork"
    }
}

internal val mainActivityNavigateToNativePremiumUpsellFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returns("V")
    custom { methodDef, classDef ->
        classDef.endsWith("MainActivity;") && methodDef.name == "navigateToNativePremiumUpsell"
    }
}
