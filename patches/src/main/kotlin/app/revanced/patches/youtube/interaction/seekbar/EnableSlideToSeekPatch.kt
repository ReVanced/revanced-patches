package app.revanced.patches.youtube.interaction.seekbar

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.is_19_17_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.findInstructionIndicesReversed
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/SlideToSeekPatch;"

val enableSlideToSeekPatch = bytecodePatch(
    description = "Adds an option to enable slide to seek " +
        "instead of playing at 2x speed when pressing and holding in the video player."
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
        versionCheckPatch,
    )

    execute {
        addResources("youtube", "interaction.seekbar.enableSlideToSeekPatch")

        PreferenceScreen.SEEKBAR.addPreferences(
            SwitchPreference("revanced_slide_to_seek"),
        )

        var modifiedMethods = false

        // Restore the behaviour to slide to seek.

        val checkIndex = slideToSeekFingerprint.patternMatch!!.startIndex
        val checkReference = slideToSeekFingerprint.method.getInstruction(checkIndex)
            .getReference<MethodReference>()!!

        val extensionMethodDescriptor = "$EXTENSION_CLASS_DESCRIPTOR->isSlideToSeekDisabled(Z)Z"

        // A/B check method was only called on this class.
        slideToSeekFingerprint.classDef.methods.forEach { method ->
            method.findInstructionIndicesReversed {
                opcode == Opcode.INVOKE_VIRTUAL && getReference<MethodReference>() == checkReference
            }.forEach { index ->
                method.apply {
                    val register = getInstruction<OneRegisterInstruction>(index + 1).registerA

                    addInstructions(
                        index + 2,
                        """
                            invoke-static { v$register }, $extensionMethodDescriptor
                            move-result v$register
                       """,
                    )
                }

                modifiedMethods = true
            }
        }

        if (!modifiedMethods) throw PatchException("Could not find methods to modify")

        // Disable the double speed seek gesture.
        if (is_19_17_or_greater) {
            disableFastForwardGestureFingerprint.let {
                it.method.apply {
                    val targetIndex = it.patternMatch!!.endIndex
                    val targetRegister = getInstruction<OneRegisterInstruction>(targetIndex).registerA

                    addInstructions(
                        targetIndex + 1,
                        """
                            invoke-static { v$targetRegister }, $extensionMethodDescriptor
                            move-result v$targetRegister
                        """,
                    )
                }
            }
        } else {
            disableFastForwardLegacyFingerprint.method.apply {
                val insertIndex = disableFastForwardLegacyFingerprint.patternMatch!!.endIndex + 1
                val targetRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                addInstructions(
                    insertIndex,
                    """
                        invoke-static { v$targetRegister }, $extensionMethodDescriptor
                        move-result v$targetRegister
                    """,
                )
            }
        }
    }
}
