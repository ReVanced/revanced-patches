package app.revanced.patches.myfitnesspal.ads.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object MainActivityNavigateToNativePremiumUpsellFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = 18,
    customFingerprint = { methodDef, classDef ->
        classDef.type.endsWith("myfitnesspal/feature/main/ui/MainActivity;") &&
                methodDef.name == "navigateToNativePremiumUpsell"
    }
)
