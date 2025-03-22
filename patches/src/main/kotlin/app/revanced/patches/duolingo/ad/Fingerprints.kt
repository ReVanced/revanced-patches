package app.revanced.patches.duolingo.ad

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val initializeMonetizationDebugSettingsFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returns("V")
    parameters(
        "Z", // disableAds
        "Z", // useDebugBilling
        "Z", // showManageSubscriptions
        "Z", // alwaysShowSuperAds
        "Lcom/duolingo/debug/FamilyQuestOverride;",
    )
    opcodes(Opcode.IPUT_BOOLEAN)
}
