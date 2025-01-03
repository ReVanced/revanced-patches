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

internal const val EXTENSION_METHOD_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/SlideToSeekPatch;->isSlideToSeekDisabled(Z)Z"

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
            "18.49.37",
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
            "19.45.38",
            "19.46.42",
            "19.47.53",
        ),
    )

    execute {
        addResources("youtube", "interaction.seekbar.enableSlideToSeekPatch")

        PreferenceScreen.SEEKBAR.addPreferences(
            SwitchPreference("revanced_slide_to_seek"),
        )

        var modifiedMethods = false

        // Restore the behaviour to slide to seek.

        val checkIndex = slideToSeekFingerprint.filterMatches.first().index
        val checkReference = slideToSeekFingerprint.method.getInstruction(checkIndex)
            .getReference<MethodReference>()!!

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
                            invoke-static { v$register }, $EXTENSION_METHOD_DESCRIPTOR
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
            arrayOf(
                disableFastForwardGestureFingerprint,
                disableFastForwardNoticeFingerprint,
            ).forEach { fingerprint ->
                fingerprint.method.apply {
                    val targetIndex = fingerprint.filterMatches.last().index
                    val targetRegister = getInstruction<OneRegisterInstruction>(targetIndex).registerA

                    addInstructions(
                        targetIndex + 1,
                        """
                            invoke-static { v$targetRegister }, $EXTENSION_METHOD_DESCRIPTOR
                            move-result v$targetRegister
                        """,
                    )
                }
            }
        } else {
            disableFastForwardLegacyFingerprint.method.apply {
                val insertIndex = disableFastForwardLegacyFingerprint.filterMatches.last().index + 1
                val targetRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                addInstructions(
                    insertIndex,
                    """
                        invoke-static { v$targetRegister }, $EXTENSION_METHOD_DESCRIPTOR
                        move-result v$targetRegister
                    """,
                )
            }
        }
    }
}
