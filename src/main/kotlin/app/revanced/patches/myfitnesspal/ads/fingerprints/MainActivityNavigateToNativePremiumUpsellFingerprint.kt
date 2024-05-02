package app.revanced.patches.myfitnesspal.ads.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object MainActivityNavigateToNativePremiumUpsellFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PRIVATE or AccessFlags.FINAL,
    customFingerprint = { methodDef, classDef ->
        classDef.type.endsWith("MainActivity;") && methodDef.name == "navigateToNativePremiumUpsell"
    }
)
