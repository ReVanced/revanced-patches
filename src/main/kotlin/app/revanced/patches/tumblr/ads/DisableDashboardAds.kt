package app.revanced.patches.tumblr.ads

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.tumblr.ads.fingerprints.AdWaterfallFingerprint
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Disable dashboard ads",
    description = "Disables ads in the dashboard.",
    compatiblePackages = [CompatiblePackage("com.tumblr")]
)
@Suppress("unused")
object DisableDashboardAds : BytecodePatch(
    setOf(AdWaterfallFingerprint)
) {
    override fun execute(context: BytecodeContext) = AdWaterfallFingerprint.result?.let {
        it.scanResult.stringsScanResult!!.matches.forEach { match ->
            // We just replace all occurrences of "client_side_ad_waterfall" with anything else
            // so the app fails to handle ads in the timeline elements array and just skips them.
            // See AdWaterfallFingerprint for more info.
            val stringRegister = it.mutableMethod.getInstruction<OneRegisterInstruction>(match.index).registerA
            it.mutableMethod.replaceInstruction(
                match.index, "const-string v$stringRegister, \"dummy\""
            )
        }
    } ?: throw AdWaterfallFingerprint.exception
}