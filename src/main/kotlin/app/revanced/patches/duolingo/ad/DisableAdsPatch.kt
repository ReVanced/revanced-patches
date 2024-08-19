package app.revanced.patches.duolingo.ad

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.duolingo.ad.fingerprints.InitializeMonetizationDebugSettingsFingerprint
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Patch(
    name = "Disable ads",
    compatiblePackages = [CompatiblePackage("com.duolingo")]
)
@Suppress("unused")
object DisableAdsPatch : BytecodePatch(
    setOf(InitializeMonetizationDebugSettingsFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        // Couple approaches to remove ads exist:
        //
        // MonetizationDebugSettings has a boolean value for "disableAds".
        // OnboardingState has a getter to check if the user has any "adFreeSessions".
        // SharedPreferences has a debug boolean value with key "disable_ads", which maps to "DebugCategory.DISABLE_ADS".
        //
        // MonetizationDebugSettings seems to be the most general setting to work fine.
        InitializeMonetizationDebugSettingsFingerprint.resultOrThrow().let {
            it.mutableMethod.apply {
                val insertIndex = it.scanResult.patternScanResult!!.startIndex
                val register = getInstruction<TwoRegisterInstruction>(insertIndex).registerA

                addInstructions(
                    insertIndex,
                    "const/4 v$register, 0x1"
                )
            }
        }
    }
}