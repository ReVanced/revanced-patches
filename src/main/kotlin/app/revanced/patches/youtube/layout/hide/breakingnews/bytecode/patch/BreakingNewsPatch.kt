package app.revanced.patches.youtube.layout.hide.breakingnews.bytecode.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.layout.hide.breakingnews.annotations.BreakingNewsCompatibility
import app.revanced.patches.youtube.layout.hide.breakingnews.bytecode.fingerprints.BreakingNewsFingerprint
import app.revanced.patches.youtube.layout.hide.breakingnews.resource.patch.BreakingNewsResourcePatch
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction

@Patch
@DependsOn([IntegrationsPatch::class, BreakingNewsResourcePatch::class])
@Name("hide-breaking-news-shelf")
@Description("Hides the breaking news shelf on the homepage tab.")
@BreakingNewsCompatibility
@Version("0.0.1")
class BreakingNewsPatch : BytecodePatch(
    listOf(BreakingNewsFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        BreakingNewsFingerprint.result?.let {
            val insertIndex = it.scanResult.patternScanResult!!.endIndex - 1
            val moveResultIndex = insertIndex - 1

            it.mutableMethod.apply {
                val breakingNewsViewRegister = instruction<OneRegisterInstruction>(moveResultIndex).registerA

                addInstruction(
                    insertIndex,
                    """
                        invoke-static {v$breakingNewsViewRegister}, 
                        Lapp/revanced/integrations/patches/HideBreakingNewsPatch;
                        ->
                        hideBreakingNews(Landroid/view/View;)V
                    """
                )
            }

        } ?: return BreakingNewsFingerprint.toErrorResult()


        return PatchResultSuccess()
    }
}
