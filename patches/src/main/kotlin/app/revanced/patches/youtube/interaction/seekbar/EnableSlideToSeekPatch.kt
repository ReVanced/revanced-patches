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
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.sun.org.apache.bcel.internal.generic.InstructionConst.getInstruction

internal const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/SlideToSeekPatch;"

@Suppress("unused")
val enableSlideToSeekPatch = bytecodePatch(
    name = "Enable slide to seek",
    description = "Adds an option to enable slide to seek " +
        "instead of playing at 2x speed when pressing and holding in the video player. " +
        "Including this patch may cause issues with tapping or double tapping the video player overlay.",
    use = false,
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
        versionCheckPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "18.38.44",
            "18.49.37",
            "19.16.39",
            "19.25.37",
            "19.34.42",
        ),
    )

    val slideToSeekMatch by slideToSeekFingerprint()
    val disableFastForwardLegacyMatch by disableFastForwardLegacyFingerprint()
    val disableFastForwardGestureMatch by disableFastForwardGestureFingerprint()
    val disableFastForwardNoticeMatch by disableFastForwardNoticeFingerprint()

    execute {
        addResources("youtube", "interaction.seekbar.enableSlideToSeekPatch")

        PreferenceScreen.SEEKBAR.addPreferences(
            SwitchPreference("revanced_slide_to_seek"),
        )

        var modifiedMethods = false

        // Restore the behaviour to slide to seek.
        val checkIndex =
            slideToSeekMatch.patternMatch!!.startIndex
        val checkReference =
            slideToSeekMatch.mutableMethod.getInstruction(checkIndex).getReference<MethodReference>()!!

        // A/B check method was only called on this class.
        slideToSeekMatch.mutableClass.methods.forEach { method ->
            method.implementation!!.instructions.forEachIndexed { index, instruction ->
                if (instruction.opcode == Opcode.INVOKE_VIRTUAL &&
                    instruction.getReference<MethodReference>() == checkReference
                ) {
                    method.apply {
                        val targetRegister = getInstruction<OneRegisterInstruction>(index + 1).registerA

                        addInstructions(
                            index + 2,
                            """
                                invoke-static { v$targetRegister }, $EXTENSION_CLASS_DESCRIPTOR
                                move-result v$targetRegister
                           """,
                        )
                    }

                    modifiedMethods = true
                }
            }
        }

        if (!modifiedMethods) throw PatchException("Could not find methods to modify")

        // Disable the double speed seek gesture.
        if (!is_19_17_or_greater) {
            disableFastForwardLegacyMatch.mutableMethod.apply {
                val insertIndex = disableFastForwardLegacyMatch.patternMatch!!.endIndex + 1
                val targetRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                addInstructions(
                    insertIndex,
                    """
                        invoke-static { v$targetRegister }, $EXTENSION_CLASS_DESCRIPTOR
                        move-result v$targetRegister
                    """,
                )
            }
        } else {
            arrayOf(
                disableFastForwardGestureMatch,
                disableFastForwardNoticeMatch,
            ).forEach {
                it.mutableMethod.apply {
                    val targetIndex = it.patternMatch!!.endIndex
                    val targetRegister = getInstruction<OneRegisterInstruction>(targetIndex).registerA

                    addInstructions(
                        targetIndex + 1,
                        """
                            invoke-static { v$targetRegister }, $EXTENSION_CLASS_DESCRIPTOR
                            move-result v$targetRegister
                        """,
                    )
                }
            }
        }
    }
}
