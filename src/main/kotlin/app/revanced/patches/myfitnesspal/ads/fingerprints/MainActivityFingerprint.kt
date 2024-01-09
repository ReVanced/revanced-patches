package app.revanced.patches.myfitnesspal.ads.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.extensions.or

object MainActivityNavigateToNativePremiumUpsellFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PRIVATE or AccessFlags.FINAL,
    customFingerprint = { methodDef, classDef ->
        classDef.type.endsWith("myfitnesspal/feature/main/ui/MainActivity;") &&
                methodDef.name == "navigateToNativePremiumUpsell"
    }
)
