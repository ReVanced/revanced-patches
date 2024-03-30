package app.revanced.patches.instagram.patches.ads.timeline

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.instagram.patches.ads.timeline.fingerprints.IsAdCheckOneFingerprint
import app.revanced.patches.instagram.patches.ads.timeline.fingerprints.IsAdCheckTwoFingerprint
import app.revanced.patches.instagram.patches.ads.timeline.fingerprints.ShowAdFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction

@Patch(
    name = "Hide timeline ads",
    compatiblePackages = [CompatiblePackage("com.instagram.android")],
)
@Suppress("unused")
object HideTimelineAdsPatch : BytecodePatch(
    setOf(
        ShowAdFingerprint,
        IsAdCheckOneFingerprint,
        IsAdCheckTwoFingerprint,
    ),
) {
    override fun execute(context: BytecodeContext) {
        // The exact function of the following methods is unknown.
        // They are used to check if a post is an ad.
        val isAdCheckOneMethod = IsAdCheckOneFingerprint.result?.method ?: throw IsAdCheckOneFingerprint.exception
        val isAdCheckTwoMethod = IsAdCheckTwoFingerprint.result?.method ?: throw IsAdCheckTwoFingerprint.exception

        ShowAdFingerprint.result?.let {
            it.mutableMethod.apply {
                // The register that holds the post object.
                val postRegister = getInstruction<FiveRegisterInstruction>(1).registerC

                // At this index the check for an ad can be performed.
                val checkIndex = it.scanResult.patternScanResult!!.endIndex

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
                    ExternalLabel("not_an_ad", getInstruction(checkIndex)),
                )
            }
        } ?: throw ShowAdFingerprint.exception
    }
}
