package app.revanced.patches.youtube.layout.seekbar

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.youtube.layout.seekbar.fingerprints.playerSeekbarColorFingerprint
import app.revanced.patches.youtube.layout.seekbar.fingerprints.setSeekbarClickedColorFingerprint
import app.revanced.patches.youtube.layout.seekbar.fingerprints.shortsSeekbarColorFingerprint
import app.revanced.patches.youtube.layout.theme.lithoColorHookPatch
import app.revanced.patches.youtube.layout.theme.lithoColorOverrideHook
import app.revanced.patches.youtube.misc.integrations.integrationsPatch
import app.revanced.util.indexOfFirstWideLiteralInstructionValue
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

private const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/youtube/patches/theme/SeekbarColorPatch;"

val seekbarColorBytecodePatch = bytecodePatch(
    description = "Hide or set a custom seekbar color",
) {
    dependsOn(
        integrationsPatch,
        lithoColorHookPatch,
        seekbarColorResourcePatch,
    )

    val playerSeekbarColorResult by playerSeekbarColorFingerprint
    val shortsSeekbarColorResult by shortsSeekbarColorFingerprint
    val setSeekbarClickedColorResult by setSeekbarClickedColorFingerprint

    execute { context ->
        fun MutableMethod.addColorChangeInstructions(resourceId: Long) {
            val registerIndex = indexOfFirstWideLiteralInstructionValue(resourceId) + 2
            val colorRegister = getInstruction<OneRegisterInstruction>(registerIndex).registerA
            addInstructions(
                registerIndex + 1,
                """
                    invoke-static { v$colorRegister }, $INTEGRATIONS_CLASS_DESCRIPTOR->getVideoPlayerSeekbarColor(I)I
                    move-result v$colorRegister
                """,
            )
        }

        playerSeekbarColorResult.mutableMethod.apply {
            addColorChangeInstructions(inlineTimeBarColorizedBarPlayedColorDarkId)
            addColorChangeInstructions(inlineTimeBarPlayedNotHighlightedColorId)
        }

        shortsSeekbarColorResult.mutableMethod.apply {
            addColorChangeInstructions(reelTimeBarPlayedColorId)
        }

        setSeekbarClickedColorResult.mutableMethod.let {
            val setColorMethodIndex = setSeekbarClickedColorResult.scanResult.patternScanResult!!.startIndex + 1
            val method = context.navigate(it).at(setColorMethodIndex).mutable()

            method.apply {
                val colorRegister = getInstruction<TwoRegisterInstruction>(0).registerA
                addInstructions(
                    0,
                    """
                        invoke-static { v$colorRegister }, $INTEGRATIONS_CLASS_DESCRIPTOR->getVideoPlayerSeekbarClickedColor(I)I
                        move-result v$colorRegister
                    """,
                )
            }
        }

        lithoColorOverrideHook(INTEGRATIONS_CLASS_DESCRIPTOR, "getLithoColor")
    }
}
