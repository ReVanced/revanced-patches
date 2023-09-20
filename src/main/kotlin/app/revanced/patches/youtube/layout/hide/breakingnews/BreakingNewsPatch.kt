package app.revanced.patches.youtube.layout.hide.breakingnews

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.layout.hide.breakingnews.fingerprints.BreakingNewsFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Hide breaking news shelf",
    description = "Hides the breaking news shelf on the homepage tab.",
    dependencies = [
        IntegrationsPatch::class,
        BreakingNewsResourcePatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.16.37",
                "18.19.35",
                "18.20.39",
                "18.23.35",
                "18.29.38",
                "18.32.39"
            ]
        )
    ]
)
@Suppress("unused")
object BreakingNewsPatch : BytecodePatch(
    setOf(BreakingNewsFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        BreakingNewsFingerprint.result?.let {
            val insertIndex = it.scanResult.patternScanResult!!.endIndex - 1
            val moveResultIndex = insertIndex - 1

            it.mutableMethod.apply {
                val breakingNewsViewRegister =
                    getInstruction<OneRegisterInstruction>(moveResultIndex).registerA

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

        } ?: throw BreakingNewsFingerprint.exception

    }
}
