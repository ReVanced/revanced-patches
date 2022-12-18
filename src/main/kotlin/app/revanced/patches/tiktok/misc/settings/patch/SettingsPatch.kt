package app.revanced.patches.tiktok.misc.settings.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.tiktok.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.tiktok.misc.settings.annotations.SettingsCompatibility
import app.revanced.patches.tiktok.misc.settings.fingerprints.AboutViewFingerprint
import app.revanced.patches.tiktok.misc.settings.fingerprints.AdPersonalizationActivityOnCreateFingerprint
import app.revanced.patches.tiktok.misc.settings.fingerprints.SettingsOnViewCreatedFingerprint
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.instruction.formats.Instruction35c
import org.jf.dexlib2.iface.reference.StringReference
import org.jf.dexlib2.iface.reference.TypeReference

@Patch
@DependsOn([IntegrationsPatch::class])
@Name("settings")
@Description("Adds ReVanced settings to TikTok.")
@SettingsCompatibility
@Version("0.0.1")
class SettingsPatch : BytecodePatch(
    listOf(
        AdPersonalizationActivityOnCreateFingerprint,
        SettingsOnViewCreatedFingerprint,
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        SettingsOnViewCreatedFingerprint.result?.let {
            AboutViewFingerprint.resolve(context, it.method, it.classDef)
        }
        // Patch Settings UI to add 'Revanced Settings'.
        val targetIndexes = findOptionsOnClickIndex()
        with(SettingsOnViewCreatedFingerprint.result!!.mutableMethod) {
            for (index in targetIndexes) {
                if (
                    instruction(index).opcode != Opcode.NEW_INSTANCE ||
                    instruction(index - 4).opcode != Opcode.MOVE_RESULT_OBJECT
                )
                    return PatchResultError("Hardcode offset changed.")
                patchOptionNameAndOnClickEvent(index, context)
            }
        }
        // Implement settings screen in `AdPersonalizationActivity`
        with(AdPersonalizationActivityOnCreateFingerprint.result!!.mutableMethod) {
            for ((index, instruction) in implementation!!.instructions.withIndex()) {
                if (instruction.opcode != Opcode.INVOKE_SUPER) continue
                val thisRegister = (instruction as Instruction35c).registerC
                addInstructions(
                    index + 1,
                    """
                        invoke-static {v$thisRegister}, Lapp/revanced/tiktok/settingsmenu/SettingsMenu;->initializeSettings(Lcom/bytedance/ies/ugc/aweme/commercialize/compliance/personalization/AdPersonalizationActivity;)V
                        return-void
                    """
                )
                break
            }
        }
        return PatchResultSuccess()
    }

    private fun findOptionsOnClickIndex(): IntArray {
        val results = IntArray(2)
        SettingsOnViewCreatedFingerprint.result?.apply {
            for ((index, instruction) in mutableMethod.implementation!!.instructions.withIndex()) {
                // Old UI settings option to replace to 'Revanced Settings'
                if (instruction.opcode == Opcode.CONST_STRING) {
                    val string = ((instruction as ReferenceInstruction).reference as StringReference).string
                    if (string == "copyright_policy") {
                        results[0] = index - 2
                        break
                    }
                }
            }

            // New UI settings option to replace to 'Revanced Settings'
            results[1] = AboutViewFingerprint.result!!.scanResult.patternScanResult!!.startIndex
        } ?: throw SettingsOnViewCreatedFingerprint.toErrorResult()
        return results
    }

    private fun patchOptionNameAndOnClickEvent(index: Int, context: BytecodeContext) {
        with(SettingsOnViewCreatedFingerprint.result!!.mutableMethod) {
            // Patch option name
            val overrideRegister = (instruction(index - 4) as OneRegisterInstruction).registerA
            replaceInstruction(
                index - 4,
                """
                    const-string v$overrideRegister, "Revanced Settings"
                """
            )

            // Patch option OnClick Event
            with(((instruction(index) as ReferenceInstruction).reference as TypeReference).type) {
                context.findClass(this)!!.mutableClass.methods.first { it.name == "onClick" }
                    .addInstructions(
                        0,
                        """
                                 invoke-static {}, Lapp/revanced/tiktok/settingsmenu/SettingsMenu;->startSettingsActivity()V
                                 return-void
                             """
                    )
            }
        }
    }
}