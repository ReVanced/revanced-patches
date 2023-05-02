package app.revanced.patches.youtube.layout.theme.bytecode.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.layout.theme.annotations.ThemeCompatibility
import app.revanced.patches.youtube.layout.theme.bytecode.fingerprints.CreateDarkThemeSeekbarFingerprint
import app.revanced.patches.youtube.layout.theme.bytecode.fingerprints.CreateDarkThemeSeekbarFingerprint.indexOfInstructionWithSeekbarId
import app.revanced.patches.youtube.layout.theme.resource.ThemeResourcePatch
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import org.jf.dexlib2.iface.instruction.TwoRegisterInstruction

@Patch
@Name("theme")
@Description("Applies a custom theme.")
@DependsOn([ThemeLithoComponentsPatch::class, ThemeResourcePatch::class, IntegrationsPatch::class])
@ThemeCompatibility
@Version("0.0.1")
class ThemeBytecodePatch : BytecodePatch(listOf(CreateDarkThemeSeekbarFingerprint)) {
    override fun execute(context: BytecodeContext): PatchResult {
        CreateDarkThemeSeekbarFingerprint.result?.let {
            val putColorValueIndex = it.method.indexOfInstructionWithSeekbarId!! + 3

            it.mutableMethod.apply {
                val overrideRegister = (instruction(putColorValueIndex) as TwoRegisterInstruction).registerA

                addInstructions(
                    putColorValueIndex,
                    """
                        invoke-static { }, $INTEGRATIONS_CLASS_DESCRIPTOR->getSeekbarColorValue()I
                        move-result v$overrideRegister
                    """
                )
            }
        } ?: return CreateDarkThemeSeekbarFingerprint.toErrorResult()
        return PatchResultSuccess()
    }

    private companion object {
        private const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/patches/theme/ThemePatch;"
    }
}
