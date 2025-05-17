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
        // matches "Lcom/duolingo/debug/FamilyQuestOverride;" or "Lcom/duolingo/data/debug/monetization/FamilyQuestOverride;"
        "Lcom/duolingo/",
    )
    opcodes(Opcode.IPUT_BOOLEAN)
}
