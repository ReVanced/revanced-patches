package app.revanced.patches.myfitnesspal.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object MainActivity_navigateToNativePremiumUpsellFingerprint :
        MethodFingerprint(
                returnType = "V",
                accessFlags = 18,
                customFingerprint = { methodDef, _ ->
                    methodDef.definingClass.endsWith("myfitnesspal/feature/main/ui/MainActivity;") &&
                            methodDef.name == "navigateToNativePremiumUpsell"
                }
        )
