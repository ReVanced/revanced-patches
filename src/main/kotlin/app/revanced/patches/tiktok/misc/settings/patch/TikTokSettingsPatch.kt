package app.revanced.patches.tiktok.misc.settings.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.tiktok.misc.integrations.patch.TikTokIntegrationsPatch
import app.revanced.patches.tiktok.misc.settings.annotations.TikTokSettingsCompatibility
import app.revanced.patches.tiktok.misc.settings.fingerprints.AdPersonalizationActivityFingerprint
import app.revanced.patches.tiktok.misc.settings.fingerprints.CopyRightSettingsStringFingerprint
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.instruction.formats.Instruction35c
import org.jf.dexlib2.iface.reference.MethodReference
import org.jf.dexlib2.iface.reference.StringReference

@Patch
@DependsOn([TikTokIntegrationsPatch::class])
@Name("tiktok-settings")
@Description("Add settings menu to TikTok.")
@TikTokSettingsCompatibility
@Version("0.0.1")
class TikTokSettingsPatch : BytecodePatch(
    listOf(
        AdPersonalizationActivityFingerprint,
        CopyRightSettingsStringFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        //Replace string `Copyright Policy` to 'Revanced Settings` in TikTok settings.
        val method1 = CopyRightSettingsStringFingerprint.result!!.mutableMethod
        val implementation1 = method1.implementation!!
        for ((index, instruction) in implementation1.instructions.withIndex()) {
            if (instruction.opcode != Opcode.CONST_STRING) continue
            val string = ((instruction as ReferenceInstruction).reference as StringReference).string
            if (string != "copyright_policy") continue
            var targetIndex = index
            while (targetIndex >= 0) {
                targetIndex--
                val invokeInstruction = implementation1.instructions[targetIndex]
                if (invokeInstruction.opcode != Opcode.INVOKE_VIRTUAL) continue
                val methodName = ((invokeInstruction as Instruction35c).reference as MethodReference).name
                if (methodName != "getString") continue
                val resultInstruction = implementation1.instructions[targetIndex + 1]
                if (resultInstruction.opcode != Opcode.MOVE_RESULT_OBJECT) continue
                val overrideRegister = (resultInstruction as OneRegisterInstruction).registerA
                method1.replaceInstruction(
                    targetIndex + 1,
                    """
                        const-string v$overrideRegister, "Revanced Settings"
                    """
                )
                break
            }
            //Change onClick to start settings activity.
            val clickInstruction = implementation1.instructions[index - 1]
            if (clickInstruction.opcode != Opcode.INVOKE_DIRECT)
                return PatchResultError("Can not find click listener.")
            val clickClass = ((clickInstruction as ReferenceInstruction).reference as MethodReference).definingClass
            val mutableClickClass = context.findClass(clickClass)!!.mutableClass
            val mutableOnClickMethod = mutableClickClass.methods.first {
                it.name == "onClick"
            }
            mutableOnClickMethod.addInstructions(
                0,
                """
                    invoke-static {}, Lapp/revanced/tiktok/settingsmenu/SettingsMenu;->startSettingsActivity()V
                    return-void
                """
            )
            break
        }
        //Implement revanced settings screen in `AdPersonalizationActivity`
        val method2 = AdPersonalizationActivityFingerprint.result!!.mutableMethod
        for ((index, instruction) in method2.implementation!!.instructions.withIndex()) {
            if (instruction.opcode != Opcode.INVOKE_SUPER) continue
            val thisRegister = (instruction as Instruction35c).registerC
            method2.addInstructions(
                index + 1,
                """
                    invoke-static {v$thisRegister}, Lapp/revanced/tiktok/settingsmenu/SettingsMenu;->initializeSettings(Lcom/bytedance/ies/ugc/aweme/commercialize/compliance/personalization/AdPersonalizationActivity;)V
                    return-void
                """
            )
            break
        }
        return PatchResultSuccess()
    }
}