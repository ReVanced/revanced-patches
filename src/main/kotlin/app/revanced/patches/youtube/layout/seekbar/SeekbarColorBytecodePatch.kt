package app.revanced.patches.youtube.layout.seekbar

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.youtube.layout.seekbar.fingerprints.LithoLinearGradientFingerprint
import app.revanced.patches.youtube.layout.seekbar.fingerprints.PlayerSeekbarColorFingerprint
import app.revanced.patches.youtube.layout.seekbar.fingerprints.PlayerSeekbarGradientConfigFingerprint
import app.revanced.patches.youtube.layout.seekbar.fingerprints.SetSeekbarClickedColorFingerprint
import app.revanced.patches.youtube.layout.seekbar.fingerprints.ShortsSeekbarColorFingerprint
import app.revanced.patches.youtube.layout.theme.LithoColorHookPatch
import app.revanced.patches.youtube.layout.theme.LithoColorHookPatch.lithoColorOverrideHook
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.playservice.VersionCheckPatch
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstWideLiteralInstructionValueOrThrow
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Patch(
    description = "Hide or set a custom seekbar color",
    dependencies = [IntegrationsPatch::class, LithoColorHookPatch::class, SeekbarColorResourcePatch::class],
    compatiblePackages = [CompatiblePackage("com.google.android.youtube")]
)
internal object SeekbarColorBytecodePatch : BytecodePatch(
    setOf(
        PlayerSeekbarColorFingerprint,
        ShortsSeekbarColorFingerprint,
        SetSeekbarClickedColorFingerprint,
        PlayerSeekbarGradientConfigFingerprint,
        LithoLinearGradientFingerprint
    )
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/youtube/patches/theme/SeekbarColorPatch;"

    override fun execute(context: BytecodeContext) {
        fun MutableMethod.addColorChangeInstructions(resourceId: Long) {
            val registerIndex = indexOfFirstWideLiteralInstructionValueOrThrow(resourceId) + 2
            val colorRegister = getInstruction<OneRegisterInstruction>(registerIndex).registerA

            addInstructions(
                registerIndex + 1,
                """
                    invoke-static { v$colorRegister }, $INTEGRATIONS_CLASS_DESCRIPTOR->getVideoPlayerSeekbarColor(I)I
                    move-result v$colorRegister
                """
            )
        }

        PlayerSeekbarColorFingerprint.resultOrThrow().mutableMethod.apply {
            addColorChangeInstructions(SeekbarColorResourcePatch.inlineTimeBarColorizedBarPlayedColorDarkId)
            addColorChangeInstructions(SeekbarColorResourcePatch.inlineTimeBarPlayedNotHighlightedColorId)
        }

        ShortsSeekbarColorFingerprint.resultOrThrow().mutableMethod.apply {
            addColorChangeInstructions(SeekbarColorResourcePatch.reelTimeBarPlayedColorId)
        }

        SetSeekbarClickedColorFingerprint.resultOrThrow().let { result ->
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
        }

        if (VersionCheckPatch.is_19_23_or_greater) {
            PlayerSeekbarGradientConfigFingerprint.resultOrThrow().mutableMethod.apply {
                val literalIndex = indexOfFirstWideLiteralInstructionValueOrThrow(
                    PlayerSeekbarGradientConfigFingerprint.PLAYER_SEEKBAR_GRADIENT_FEATURE_FLAG
                )
                val resultIndex = indexOfFirstInstructionOrThrow(literalIndex, Opcode.MOVE_RESULT)
                val register = getInstruction<OneRegisterInstruction>(resultIndex).registerA

                addInstructions(
                    resultIndex + 1,
                    """
                        invoke-static { v$register }, $INTEGRATIONS_CLASS_DESCRIPTOR->playerSeekbarGradientEnabled(Z)Z
                        move-result v$register
                    """
                )
            }

            LithoLinearGradientFingerprint.resultOrThrow().mutableMethod.apply {
                addInstruction(0, "invoke-static/range { p4 .. p5 },  " +
                        "$INTEGRATIONS_CLASS_DESCRIPTOR->setLinearGradient([I[F)V"
                )
            }
        }

        lithoColorOverrideHook(INTEGRATIONS_CLASS_DESCRIPTOR, "getLithoColor")
    }
}
