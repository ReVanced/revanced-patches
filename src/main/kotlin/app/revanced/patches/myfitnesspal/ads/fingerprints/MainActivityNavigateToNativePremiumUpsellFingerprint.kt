package app.revanced.patches.myfitnesspal.ads.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

val mainActivityNavigateToNativePremiumUpsellFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returns("V")
    custom { methodDef, classDef ->
        classDef.type.endsWith("MainActivity;") && methodDef.name == "navigateToNativePremiumUpsell"
    }
}
