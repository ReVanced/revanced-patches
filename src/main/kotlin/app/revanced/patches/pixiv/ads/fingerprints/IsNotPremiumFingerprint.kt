package app.revanced.patches.pixiv.ads.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags


object IsNotPremiumFingerprint : MethodFingerprint(
    "V",
    AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    listOf("L"),
    strings = listOf("pixivAccountManager"),
    customFingerprint = custom@{ _, classDef ->
        // The "isNotPremium" method is the only method in the class.
        if (classDef.virtualMethods.count() != 1) return@custom false

        classDef.virtualMethods.first().let { isNotPremiumMethod ->
            isNotPremiumMethod.parameterTypes.size == 0 && isNotPremiumMethod.returnType == "Z"
        }
    }
)