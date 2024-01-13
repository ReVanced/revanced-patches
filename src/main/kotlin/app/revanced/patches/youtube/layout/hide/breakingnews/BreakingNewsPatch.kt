package app.revanced.patches.youtube.layout.hide.breakingnews

import app.revanced.util.exception
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
    description = "Adds an option to hide the breaking news shelf on the homepage tab.",
    dependencies = [
        IntegrationsPatch::class,
        BreakingNewsResourcePatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.32.39",
                "18.37.36",
                "18.38.44",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.48.39",
                "18.49.37",
                "19.01.34"
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
                        Lapp/revanced/integrations/youtube/patches/HideBreakingNewsPatch;
                        ->
                        hideBreakingNews(Landroid/view/View;)V
                    """
                )
            }

        } ?: throw BreakingNewsFingerprint.exception

    }
}
