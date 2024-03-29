package app.revanced.patches.youtube.layout.hide.suggestionsshelf

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.layout.hide.suggestionsshelf.fingerprints.BreakingNewsFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.litho.filter.LithoFilterPatch
import app.revanced.patches.youtube.misc.navigation.NavigationBarHookPatch
import app.revanced.patches.youtube.misc.playertype.PlayerTypeHookPatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Hide Suggestions shelf",
    description = "Hides suggestions shelf on the homepage tab.",
    dependencies = [
        IntegrationsPatch::class,
        HideSuggestionsShelfResourcePatch::class,
        NavigationBarHookPatch::class,
        LithoFilterPatch::class,
        PlayerTypeHookPatch::class,
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
                "19.01.34",
                "19.02.39",
                "19.03.36",
                "19.04.38",
                "19.05.36",
                "19.06.39",
                "19.07.40",
                "19.08.36",
                "19.09.37"
            ]
        )
    ]
)
@Suppress("unused")
object HideSuggestionsShelfPatch : BytecodePatch(
    setOf(BreakingNewsFingerprint)
) {
    private const val FILTER_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/components/SuggestionsShelfFilter;"

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
                        invoke-static {v$breakingNewsViewRegister}, $FILTER_CLASS_DESCRIPTOR->hideBreakingNews(Landroid/view/View;)V
                    """
                )
            }

        } ?: throw BreakingNewsFingerprint.exception

        LithoFilterPatch.addFilter(FILTER_CLASS_DESCRIPTOR)
    }
}
