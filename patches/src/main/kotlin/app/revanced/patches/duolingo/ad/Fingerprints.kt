package app.revanced.patches.duolingo.ad

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal fun buildInitMonetizationFingerprint(lastParam: String) = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returns("V")
    parameters(
        "Z", // disableAds
        "Z", // useDebugBilling
        "Z", // showManageSubscriptions
        "Z", // alwaysShowSuperAds
        lastParam,
    )
    opcodes(Opcode.IPUT_BOOLEAN)
}
