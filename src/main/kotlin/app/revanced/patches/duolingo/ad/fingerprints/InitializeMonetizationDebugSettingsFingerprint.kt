package app.revanced.patches.duolingo.ad.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object InitializeMonetizationDebugSettingsFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    parameters = listOf(
        "Z", // disableAds
        "Z", // useDebugBilling
        "Z", // showManageSubscriptions
        "Z", // alwaysShowSuperAds
        "Lcom/duolingo/debug/FamilyQuestOverride;",
    ),
    opcodes = listOf(
        Opcode.IPUT_BOOLEAN
    )
)
