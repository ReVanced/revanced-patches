package app.revanced.patches.youtube.shorts.shortscomponent

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.shorts.shortscomponent.fingerprints.ToolBarBannerFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.SHORTS
import app.revanced.patches.youtube.utils.toolbar.ToolBarHookPatch
import app.revanced.util.exception

@Patch(dependencies = [ToolBarHookPatch::class])
object ShortsToolBarPatch : BytecodePatch(
    setOf(ToolBarBannerFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        ToolBarBannerFingerprint.result?.let {
            val targetMethod = context
                .toMethodWalker(it.method)
                .nextMethod(it.scanResult.patternScanResult!!.endIndex, true)
                .getMethod() as MutableMethod

            targetMethod.apply {
                addInstructionsWithLabels(
                    0,
                    """
                        invoke-static {}, $SHORTS->hideShortsToolBarBanner()Z
                        move-result v0
                        if-nez v0, :hide
                        """,
                    ExternalLabel("hide", getInstruction(implementation!!.instructions.size - 1))
                )
            }
        } ?: throw ToolBarBannerFingerprint.exception

        ToolBarHookPatch.injectCall("$SHORTS->hideShortsToolBarButton")
    }
}
