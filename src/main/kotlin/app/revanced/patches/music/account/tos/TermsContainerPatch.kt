package app.revanced.patches.music.account.tos

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.account.tos.fingerprints.TermsOfServiceFingerprint
import app.revanced.patches.music.utils.integrations.Constants.ACCOUNT
import app.revanced.patches.music.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.music.utils.settings.CategoryType
import app.revanced.patches.music.utils.settings.SettingsPatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c

@Patch(
    name = "Hide terms container",
    description = "Adds an option to hide the terms of service container in the account menu.",
    dependencies = [
        SettingsPatch::class,
        SharedResourceIdPatch::class
    ],
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")]
)
@Suppress("unused")
object TermsContainerPatch : BytecodePatch(
    setOf(TermsOfServiceFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        TermsOfServiceFingerprint.result?.let {
            it.mutableMethod.apply {
                var insertIndex = 0

                for (index in implementation!!.instructions.size - 1 downTo 0) {
                    if (getInstruction(index).opcode != Opcode.INVOKE_VIRTUAL) continue

                    val targetReference =
                        getInstruction<ReferenceInstruction>(index).reference.toString()

                    if (targetReference.endsWith("/PrivacyTosFooter;->setVisibility(I)V")) {
                        insertIndex = index

                        val visibilityRegister =
                            getInstruction<Instruction35c>(insertIndex).registerD

                        addInstruction(
                            index + 1,
                            "const/4 v$visibilityRegister, 0x0"
                        )
                        addInstructions(
                            index, """
                                invoke-static {}, $ACCOUNT->hideTermsContainer()I
                                move-result v$visibilityRegister
                                """
                        )

                        break
                    }
                }
                if (insertIndex == 0)
                    throw PatchException("target Instruction not found!")
            }
        } ?: throw TermsOfServiceFingerprint.exception

        SettingsPatch.addMusicPreference(
            CategoryType.ACCOUNT,
            "revanced_hide_terms_container",
            "false"
        )

    }
}
