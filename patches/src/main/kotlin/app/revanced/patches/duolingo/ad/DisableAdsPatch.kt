package app.revanced.patches.duolingo.ad

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Suppress("unused")
val disableAdsPatch = bytecodePatch(
    "Disable ads",
) {
    // 6.55.3 and higher can show ads after each exercise.
    compatibleWith("com.duolingo"("6.54.5"))

    execute {
        // Couple approaches to remove ads exist:
        //
        // MonetizationDebugSettings has a boolean value for "disableAds".
        // OnboardingState has a getter to check if the user has any "adFreeSessions".
        // SharedPreferences has a debug boolean value with key "disable_ads", which maps to "DebugCategory.DISABLE_ADS".
        //
        // MonetizationDebugSettings seems to be the most general setting to work fine.
        initializeMonetizationDebugSettingsFingerprint
            .match(monetizationDebugSettingsToStringFingerprint.classDef)
            .method.apply {
                val insertIndex = initializeMonetizationDebugSettingsFingerprint.patternMatch!!.startIndex
                val register = getInstruction<TwoRegisterInstruction>(insertIndex).registerA

                addInstructions(
                    insertIndex,
                    "const/4 v$register, 0x1",
                )
            }
    }
}
