package app.revanced.patches.shared.patch.ads

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.shared.fingerprints.ads.LegacyAdsFingerprint
import app.revanced.patches.shared.fingerprints.ads.MainstreamAdsFingerprint
import app.revanced.util.exception

abstract class AbstractAdsPatch(
    private val descriptor: String
) : BytecodePatch(
    setOf(
        LegacyAdsFingerprint,
        MainstreamAdsFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        LegacyAdsFingerprint.result?.let {
            (context.toMethodWalker(it.method)
                .nextMethod(13, true)
                .getMethod() as MutableMethod).apply {
                addInstructions(
                    0, """
                        invoke-static {}, $descriptor
                        move-result v1
                        """
                )
            }
        } ?: throw LegacyAdsFingerprint.exception

        MainstreamAdsFingerprint.result?.let {
            it.mutableMethod.apply {
                addInstructionsWithLabels(
                    0, """
                        invoke-static {}, $descriptor
                        move-result v0
                        if-nez v0, :show_ads
                        return-void
                        """, ExternalLabel("show_ads", getInstruction(0))
                )
            }
        }

    }
}