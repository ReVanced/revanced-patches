package app.revanced.patches.pixiv.ads.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags


internal val isNotPremiumFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC,AccessFlags.CONSTRUCTOR)
    returns("V")
    parameters("L")
    strings("pixivAccountManager")
    custom custom@{ _, classDef ->
        // The "isNotPremium" method is the only method in the class.
        if (classDef.virtualMethods.count() != 1) return@custom false

        classDef.virtualMethods.first().let { isNotPremiumMethod ->
            isNotPremiumMethod.parameterTypes.size == 0 && isNotPremiumMethod.returnType == "Z"
        }
    }
}