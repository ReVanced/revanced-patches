package app.revanced.patches.music.general.tooltip

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.general.tooltip.fingerprints.TooltipContentViewFingerprint
import app.revanced.patches.music.utils.resourceid.SharedResourceIdPatch
import app.revanced.util.exception

@Patch(
    name = "Hide tooltip content",
    description = "Hides the tooltip box that appears when opening the app for the first time.",
    dependencies = [SharedResourceIdPatch::class],
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")]
)
@Suppress("unused")
object TooltipContentViewPatch : BytecodePatch(
    setOf(TooltipContentViewFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        TooltipContentViewFingerprint.result?.mutableMethod?.addInstruction(
            0,
            "return-void"
        ) ?: throw TooltipContentViewFingerprint.exception

    }
}
