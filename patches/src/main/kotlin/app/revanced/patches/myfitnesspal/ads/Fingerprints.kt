package app.revanced.patches.myfitnesspal.ads

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val isPremiumUseCaseImplFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC)
    custom { method, classDef ->
        classDef.endsWith("IsPremiumUseCaseImpl;") && method.name == "doWork"
    }
}

internal val mainActivityNavigateToNativePremiumUpsellFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returns("V")
    custom { method, classDef ->
        classDef.endsWith("MainActivity;") && method.name == "navigateToNativePremiumUpsell"
    }
}
