package app.revanced.patches.youtube.interaction.seekbar.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.interaction.seekbar.annotation.SeekbarTappingCompatibility
import app.revanced.patches.youtube.interaction.seekbar.fingerprints.SeekbarTappingFingerprint
import app.revanced.patches.youtube.interaction.seekbar.fingerprints.SeekbarTappingParentFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.settings.framework.components.impl.StringResource
import app.revanced.patches.youtube.misc.settings.framework.components.impl.SwitchPreference
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.builder.instruction.BuilderInstruction21t
import org.jf.dexlib2.iface.Method
import org.jf.dexlib2.iface.instruction.formats.Instruction11n
import org.jf.dexlib2.iface.instruction.formats.Instruction35c

@Patch
@DependsOn([IntegrationsPatch::class, SettingsPatch::class])
@Name("seekbar-tapping")
@Description("Enables tap-to-seek on the seekbar of the video player.")
@SeekbarTappingCompatibility
@Version("0.0.1")
class EnableSeekbarTappingPatch : BytecodePatch(
    listOf(
        SeekbarTappingParentFingerprint, SeekbarTappingFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        SettingsPatch.PreferenceScreen.INTERACTIONS.addPreferences(
            SwitchPreference(
                "revanced_enable_tap_seeking",
                StringResource("revanced_seekbar_tapping_enabled_title", "Enable seekbar tapping"),
                true,
                StringResource("revanced_seekbar_tapping_summary_on", "Seekbar tapping is enabled"),
                StringResource("revanced_seekbar_tapping_summary_off", "Seekbar tapping is disabled")
            )
        )

        var result = SeekbarTappingParentFingerprint.result!!

        val tapSeekMethods = mutableMapOf<String, Method>()

        // find the methods which tap the seekbar
        for (it in result.classDef.methods) {
            if (it.implementation == null) continue

            val instructions = it.implementation!!.instructions
            // here we make sure we actually find the method because it has more than 7 instructions
            if (instructions.count() < 7) continue

            // we know that the 7th instruction has the opcode CONST_4
            val instruction = instructions.elementAt(6)
            if (instruction.opcode != Opcode.CONST_4) continue

            // the literal for this instruction has to be either 1 or 2
            val literal = (instruction as Instruction11n).narrowLiteral

            // method founds
            if (literal == 1) tapSeekMethods["P"] = it
            if (literal == 2) tapSeekMethods["O"] = it
        }

        // replace map because we don't need the upper one anymore
        result = SeekbarTappingFingerprint.result!!

        val implementation = result.mutableMethod.implementation!!

        // if tap-seeking is enabled, do not invoke the two methods below
        val pMethod = tapSeekMethods["P"]!!
        val oMethod = tapSeekMethods["O"]!!

        val insertIndex = result.scanResult.patternScanResult!!.endIndex + 1

        // get the required register
        val instruction = implementation.instructions[insertIndex - 1]
        if (instruction.opcode != Opcode.INVOKE_VIRTUAL) return PatchResultError("Could not find the correct register")
        val register = (instruction as Instruction35c).registerC

        val elseLabel = implementation.newLabelForIndex(insertIndex)
        // the instructions are written in reverse order.
        result.mutableMethod.addInstructions(
            insertIndex, """
               invoke-virtual { v$register, v2 }, ${oMethod.definingClass}->${oMethod.name}(I)V
               invoke-virtual { v$register, v2 }, ${pMethod.definingClass}->${pMethod.name}(I)V
            """
        )

        // if tap-seeking is disabled, do not invoke the two methods above by jumping to the else label
        implementation.addInstruction(
            insertIndex, BuilderInstruction21t(Opcode.IF_EQZ, 0, elseLabel)
        )
        result.mutableMethod.addInstructions(
            insertIndex, """
                invoke-static { }, Lapp/revanced/integrations/patches/SeekbarTappingPatch;->isTapSeekingEnabled()Z
                move-result v0
            """
        )
        return PatchResultSuccess()
    }
}
