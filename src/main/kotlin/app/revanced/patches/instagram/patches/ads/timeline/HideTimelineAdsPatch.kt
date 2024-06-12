package app.revanced.patches.instagram.patches.ads.timeline

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction

@Suppress("unused")
val hideTimelineAdsPatch = bytecodePatch(
    name = "Hide timeline ads",
) {
    compatibleWith("com.instagram.android")

    val showAdFingerprintResult by showAdFingerprint
    val isAdCheckOneFingerprintResult by isAdCheckOneFingerprint
    val isAdCheckTwoFingerprintResult by isAdCheckTwoFingerprint

    execute {
        // The exact function of the following methods is unknown.
        // They are used to check if a post is an ad.
        val isAdCheckOneMethod = isAdCheckOneFingerprintResult.method
        val isAdCheckTwoMethod = isAdCheckTwoFingerprintResult.method

        showAdFingerprintResult.mutableMethod.apply {
            // The register that holds the post object.
            val postRegister = getInstruction<FiveRegisterInstruction>(1).registerC

            // At this index the check for an ad can be performed.
            val checkIndex = showAdFingerprintResult.scanResult.patternScanResult!!.endIndex

            // If either check returns true, the post is an ad and is hidden by returning false.
            addInstructionsWithLabels(
                checkIndex,
                """
                    invoke-virtual { v$postRegister }, $isAdCheckOneMethod
                    move-result v0
                    if-nez v0, :hide_ad
                    
                    invoke-static { v$postRegister }, $isAdCheckTwoMethod
                    move-result v0
                    if-eqz v0, :not_an_ad
                    
                    :hide_ad
                    const/4 v0, 0x0 # Returning false to hide the ad.
                    return v0
                """,
                ExternalLabel("not_an_ad", instructions[checkIndex]),
            )
        }
    }
}
