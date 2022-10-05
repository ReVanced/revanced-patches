package app.revanced.patches.youtube.ad.infocardsuggestions.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.ad.infocardsuggestions.annotations.HideInfocardSuggestionsCompatibility
import app.revanced.patches.youtube.ad.infocardsuggestions.fingerprints.HideInfocardSuggestionsFingerprint
import app.revanced.patches.youtube.ad.infocardsuggestions.fingerprints.HideInfocardSuggestionsParentFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.settings.framework.components.impl.StringResource
import app.revanced.patches.youtube.misc.settings.framework.components.impl.SwitchPreference
import org.jf.dexlib2.builder.instruction.BuilderInstruction35c

@Patch
@DependsOn([IntegrationsPatch::class, SettingsPatch::class])
@Name("hide-infocard-suggestions")
@Description("Hides infocards in videos.")
@HideInfocardSuggestionsCompatibility
@Version("0.0.1")
class HideInfocardSuggestionsPatch : BytecodePatch(
    listOf(
        HideInfocardSuggestionsParentFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        SettingsPatch.PreferenceScreen.ADS.addPreferences(
            SwitchPreference(
                "revanced_info_cards_enabled",
                StringResource("revanced_info_cards_enabled_title", "Show info-cards"),
                false,
                StringResource("revanced_info_cards_enabled_summary_on", "Info-cards are shown"),
                StringResource("revanced_info_cards_enabled_summary_off", "Info-cards are hidden")
            )
        )

        val parentResult = HideInfocardSuggestionsParentFingerprint.result
            ?: return PatchResultError("Parent fingerprint not resolved!")


        HideInfocardSuggestionsFingerprint.resolve(context, parentResult.classDef)
        val result = HideInfocardSuggestionsFingerprint.result
            ?: return PatchResultError("Required parent method could not be found.")

        val method = result.mutableMethod
        val implementation = method.implementation
            ?: return PatchResultError("Implementation not found.")

        val index = implementation.instructions.indexOfFirst { ((it as? BuilderInstruction35c)?.reference.toString() == "Landroid/view/View;->setVisibility(I)V") }

        method.replaceInstruction(index, """
            invoke-static {p1}, Lapp/revanced/integrations/patches/HideInfoCardSuggestionsPatch;->hideInfoCardSuggestions(Landroid/view/View;)V
        """)

        return PatchResultSuccess()
    }

}