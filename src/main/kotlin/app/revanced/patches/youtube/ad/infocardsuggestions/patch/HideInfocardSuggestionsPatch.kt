package app.revanced.patches.youtube.ad.infocardsuggestions.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.removeInstruction
import app.revanced.patcher.fingerprint.method.utils.MethodFingerprintUtils.resolve
import app.revanced.patcher.patch.annotations.Dependencies
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patches.youtube.ad.infocardsuggestions.annotations.HideInfocardSuggestionsCompatibility
import app.revanced.patches.youtube.ad.infocardsuggestions.fingerprints.HideInfocardSuggestionsFingerprint
import app.revanced.patches.youtube.ad.infocardsuggestions.fingerprints.HideInfocardSuggestionsParentFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import org.jf.dexlib2.builder.instruction.BuilderInstruction35c
import org.jf.dexlib2.iface.instruction.FiveRegisterInstruction

@Patch
@Dependencies([IntegrationsPatch::class])
@Name("hide-infocard-suggestions")
@Description("Hides infocards in videos.")
@HideInfocardSuggestionsCompatibility
@Version("0.0.1")
class HideInfocardSuggestionsPatch : BytecodePatch(
    listOf(
        HideInfocardSuggestionsParentFingerprint
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        val parentResult = HideInfocardSuggestionsParentFingerprint.result
            ?: return PatchResultError("Parent fingerprint not resolved!")


        HideInfocardSuggestionsFingerprint.resolve(data, parentResult.classDef)
        val result = HideInfocardSuggestionsFingerprint.result
            ?: return PatchResultError("Required parent method could not be found.")

        val method = result.mutableMethod
        val implementation = method.implementation
            ?: return PatchResultError("Implementation not found.")

        val index = implementation.instructions.indexOfFirst { ((it as? BuilderInstruction35c)?.reference.toString() == "Landroid/view/View;->setVisibility(I)V") }
        val register = "v" + (implementation.instructions.get(index) as FiveRegisterInstruction).registerD

        method.removeInstruction(index)

        method.addInstructions(
            index, """
            invoke-static {}, Lapp/revanced/integrations/patches/HideInfoCardSuggestionsPatch;->hideInfoCardSuggestions()I
            move-result $register
            invoke-virtual {p1, $register}, Landroid/view/View;->setVisibility(I)V
        """
        )

        return PatchResultSuccess()
    }

}