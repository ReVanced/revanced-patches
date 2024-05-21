package app.revanced.patches.thetransitapp.misc.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object IsPremiumFingerprint : MethodFingerprint(
    // this methode is used to find the fingerprint of the method fetchSubscriptionStatus
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.STATIC,
    strings = listOf("ForceHasActiveRoyaleSubscription", "product1234"),

)