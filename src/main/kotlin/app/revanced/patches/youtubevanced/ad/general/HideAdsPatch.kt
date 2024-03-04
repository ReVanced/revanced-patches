package app.revanced.patches.youtubevanced.ad.general

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.misc.fix.verticalscroll.VerticalScrollPatch
import app.revanced.patches.youtubevanced.ad.general.fingerprints.ContainsAdFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction21c

@Deprecated("This patch is going to be removed in the future.")
@Patch(
    description = "Removes general ads.",
    dependencies = [VerticalScrollPatch::class],
    compatiblePackages = [CompatiblePackage("com.vanced.android.youtube")],
    use = false,
)
@Suppress("unused")
object HideAdsPatch : BytecodePatch(
    setOf(ContainsAdFingerprint),
) {
    override fun execute(context: BytecodeContext) {
        ContainsAdFingerprint.result?.let { result ->
            result.mutableMethod.apply {
                val insertIndex = result.scanResult.patternScanResult!!.endIndex + 1
                val adsListRegister = getInstruction<Instruction21c>(insertIndex - 2).registerA

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
                ).forEach { component ->
                    addInstructions(
                        insertIndex,
                        """
                           const-string v$adsListRegister, "$component"
                           invoke-interface {v0, v$adsListRegister}, Ljava/util/List;->add(Ljava/lang/Object;)Z
                        """,
                    )
                }
            }
        } ?: throw ContainsAdFingerprint.exception
    }
}
