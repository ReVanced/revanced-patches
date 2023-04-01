package app.revanced.patches.youtubevanced.ad.general.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.shared.misc.fix.verticalscroll.patch.VerticalScrollPatch
import app.revanced.patches.youtubevanced.ad.general.annotations.HideAdsCompatibility
import app.revanced.patches.youtubevanced.ad.general.fingerprints.ContainsAdFingerprint
import org.jf.dexlib2.iface.instruction.formats.Instruction21c

@Patch
@Name("hide-ads")
@Description("Removes general ads.")
@DependsOn([VerticalScrollPatch::class])
@HideAdsCompatibility
@Version("0.0.1")
class HideAdsPatch : BytecodePatch(
    listOf(
        ContainsAdFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        ContainsAdFingerprint.result?.let { result ->
            result.mutableMethod.apply {
                val insertIndex = result.scanResult.patternScanResult!!.endIndex + 1
                val adsListRegister = (instruction(insertIndex - 2) as Instruction21c).registerA

                listOf(
                    "_buttoned_layout",
                    "full_width_square_image_layout",
                    "_ad_with",
                    "landscape_image_wide_button_layout",
                    "banner_text_icon",
                    "cell_divider",
                    "square_image_layout",
                    "watch_metadata_app_promo",
                    "video_display_full_layout",
                    "hero_promo_image",
                    "statement_banner",
                    "primetime_promo",
                    "carousel_footered_layout",
                    "feature_grid_interstitial"
                ).forEach { component ->
                    addInstructions(
                        insertIndex, """
                           const-string v$adsListRegister, "$component"
                           invoke-interface {v0, v$adsListRegister}, Ljava/util/List;->add(Ljava/lang/Object;)Z
                        """
                    )
                }
            }
        } ?: return ContainsAdFingerprint.toErrorResult()

        return PatchResultSuccess()
    }
}
