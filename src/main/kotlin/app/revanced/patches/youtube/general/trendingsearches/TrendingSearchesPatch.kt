package app.revanced.patches.youtube.general.trendingsearches

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.general.trendingsearches.fingerprints.SearchBarEntryFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.GENERAL
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.YtOutlineArrowTimeBlack
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.YtOutlineFireBlack
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.YtOutlineSearchBlack
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.util.exception
import app.revanced.util.getWideLiteralInstructionIndex
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Patch(
    name = "Hide trending searches",
    description = "Adds an option to hide trending searches in the search bar.",
    dependencies = [
        SettingsPatch::class,
        SharedResourceIdPatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.25.40",
                "18.27.36",
                "18.29.38",
                "18.30.37",
                "18.31.40",
                "18.32.39",
                "18.33.40",
                "18.34.38",
                "18.35.36",
                "18.36.39",
                "18.37.36",
                "18.38.44",
                "18.39.41",
                "18.40.34",
                "18.41.39",
                "18.42.41",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.46.45",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39"
            ]
        )
    ]
)
@Suppress("unused")
object TrendingSearchesPatch : BytecodePatch(
    setOf(SearchBarEntryFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        SearchBarEntryFingerprint.result?.let {
            it.mutableMethod.apply {
                SearchTerm.entries
                    .map { searchTerm -> getWideLiteralInstructionIndex(searchTerm.resourceId) to searchTerm.value }
                    .sortedBy { searchTerm -> searchTerm.first }
                    .reversed()
                    .forEach { (index, value) ->
                        val freeRegister = getInstruction<OneRegisterInstruction>(index).registerA
                        val viewRegister =
                            getInstruction<TwoRegisterInstruction>(index - 1).registerA

                        addInstructions(
                            index, """
                                const/4 v$freeRegister, $value
                                invoke-static {v$viewRegister, v$freeRegister}, $GENERAL->hideTrendingSearches(Landroid/widget/ImageView;Z)V
                                """
                        )
                    }
            }
        } ?: throw SearchBarEntryFingerprint.exception

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: GENERAL_SETTINGS",
                "SETTINGS: HIDE_TRENDING_SEARCHES"
            )
        )

        SettingsPatch.updatePatchStatus("Hide trending searches")

    }

    private enum class SearchTerm(val resourceId: Long, val value: Int) {
        HISTORY(YtOutlineArrowTimeBlack, 0),
        SEARCH(YtOutlineSearchBlack, 0),
        TRENDING(YtOutlineFireBlack, 1)
    }
}