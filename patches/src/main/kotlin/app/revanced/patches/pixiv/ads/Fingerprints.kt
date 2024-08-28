package app.revanced.patches.pixiv.ads

import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint

internal val isNotPremiumFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returns("V")
    parameters("L")
    custom { _, classDef ->
        // The "isNotPremium" method is the only method in the class.
        if (classDef.virtualMethods.count() != 1) return@custom false

        classDef.virtualMethods.first().let { isNotPremiumMethod ->
            isNotPremiumMethod.parameterTypes.size == 0 && isNotPremiumMethod.returnType == "Z"
        }
    }
    strings("pixivAccountManager")
}