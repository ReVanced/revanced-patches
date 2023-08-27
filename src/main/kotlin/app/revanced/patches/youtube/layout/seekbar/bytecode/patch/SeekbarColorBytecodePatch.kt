package app.revanced.patches.youtube.layout.seekbar.bytecode.patch

import app.revanced.extensions.indexOfFirstConstantInstructionValue
import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.youtube.layout.seekbar.annotations.SeekbarColorCompatibility
import app.revanced.patches.youtube.layout.seekbar.bytecode.fingerprints.PlayerSeekbarColorFingerprint
import app.revanced.patches.youtube.layout.seekbar.bytecode.fingerprints.SetSeekbarClickedColorFingerprint
import app.revanced.patches.youtube.layout.seekbar.bytecode.fingerprints.ShortsSeekbarColorFingerprint
import app.revanced.patches.youtube.layout.seekbar.resource.SeekbarColorResourcePatch
import app.revanced.patches.youtube.layout.theme.bytecode.patch.LithoColorHookPatch
import app.revanced.patches.youtube.layout.theme.bytecode.patch.LithoColorHookPatch.Companion.lithoColorOverrideHook
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Description("Hide or set a custom seekbar color")
@DependsOn([IntegrationsPatch::class, LithoColorHookPatch::class, SeekbarColorResourcePatch::class])
@SeekbarColorCompatibility
class SeekbarColorBytecodePatch : BytecodePatch(
    listOf(PlayerSeekbarColorFingerprint, ShortsSeekbarColorFingerprint, SetSeekbarClickedColorFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        fun MutableMethod.addColorChangeInstructions(resourceId: Long) {
            var registerIndex = indexOfFirstConstantInstructionValue(resourceId) + 2
            var colorRegister = getInstruction<OneRegisterInstruction>(registerIndex).registerA
            addInstructions(
                registerIndex + 1,
                """
                        invoke-static { v$colorRegister }, $INTEGRATIONS_CLASS_DESCRIPTOR->getVideoPlayerSeekbarColor(I)I
                        move-result v$colorRegister
                    """
            )
        }

        PlayerSeekbarColorFingerprint.result?.mutableMethod?.apply {
            addColorChangeInstructions(SeekbarColorResourcePatch.inlineTimeBarColorizedBarPlayedColorDarkId)
            addColorChangeInstructions(SeekbarColorResourcePatch.inlineTimeBarPlayedNotHighlightedColorId)
        } ?: throw PlayerSeekbarColorFingerprint.exception

        ShortsSeekbarColorFingerprint.result?.mutableMethod?.apply {
            addColorChangeInstructions(SeekbarColorResourcePatch.reelTimeBarPlayedColorId)
        } ?: throw ShortsSeekbarColorFingerprint.exception

        SetSeekbarClickedColorFingerprint.result?.let { result ->
            result.mutableMethod.let {
                val setColorMethodIndex = result.scanResult.patternScanResult!!.startIndex + 1
                val method = context
                    .toMethodWalker(it)
                    .nextMethod(setColorMethodIndex, true)
                    .getMethod() as MutableMethod

                method.apply {
                    val colorRegister = getInstruction<TwoRegisterInstruction>(0).registerA
                    addInstructions(
                        0,
                        """
                            invoke-static { v$colorRegister }, $INTEGRATIONS_CLASS_DESCRIPTOR->getVideoPlayerSeekbarClickedColor(I)I
                            move-result v$colorRegister
                        """
                    )
                }
            }
        } ?: throw SetSeekbarClickedColorFingerprint.exception

        lithoColorOverrideHook(INTEGRATIONS_CLASS_DESCRIPTOR, "getLithoColor")
    }

    private companion object {
        private const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/patches/theme/SeekbarColorPatch;"
    }
}
