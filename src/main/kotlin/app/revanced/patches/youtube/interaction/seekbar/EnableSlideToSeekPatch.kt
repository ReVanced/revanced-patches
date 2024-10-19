package app.revanced.patches.youtube.interaction.seekbar

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.interaction.seekbar.fingerprints.DisableFastForwardLegacyFingerprint
import app.revanced.patches.youtube.interaction.seekbar.fingerprints.DisableFastForwardGestureFingerprint
import app.revanced.patches.youtube.interaction.seekbar.fingerprints.DisableFastForwardNoticeFingerprint
import app.revanced.patches.youtube.interaction.seekbar.fingerprints.SlideToSeekFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.playservice.VersionCheckPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.getReference
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "Enable slide to seek",
    description = "Adds an option to enable slide to seek instead of playing at 2x speed when pressing and holding in the video player. Including this patch may cause issues with the video player overlay, such as missing buttons and ignored taps and double taps.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class, AddResourcesPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.38.44",
                "18.49.37",
                "19.16.39",
                "19.25.37",
                "19.34.42",
            ]
        )
    ],
    use = false
)
@Suppress("unused")
object EnableSlideToSeekPatch : BytecodePatch(
    setOf(
        SlideToSeekFingerprint,
        DisableFastForwardLegacyFingerprint,
        DisableFastForwardGestureFingerprint,
        DisableFastForwardNoticeFingerprint
    )
) {
    private const val INTEGRATIONS_METHOD_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/SlideToSeekPatch;->isSlideToSeekDisabled(Z)Z"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.SEEKBAR.addPreferences(
            SwitchPreference("revanced_slide_to_seek")
        )

        var modifiedMethods = false

        // Restore the behaviour to slide to seek.
        SlideToSeekFingerprint.resultOrThrow().let {
            val checkIndex = it.scanResult.patternScanResult!!.startIndex
            val checkReference = it.mutableMethod
                .getInstruction(checkIndex).getReference<MethodReference>()!!

            // A/B check method was only called on this class.
            it.mutableClass.methods.forEach { method ->
                method.implementation!!.instructions.forEachIndexed { index, instruction ->
                    if (instruction.opcode == Opcode.INVOKE_VIRTUAL &&
                        instruction.getReference<MethodReference>() == checkReference
                    ) {
                        method.apply {
                            val targetRegister = getInstruction<OneRegisterInstruction>(index + 1).registerA

                            addInstructions(
                                index + 2,
                                """
                                    invoke-static { v$targetRegister }, $INTEGRATIONS_METHOD_DESCRIPTOR
                                    move-result v$targetRegister
                               """
                            )
                        }

                        modifiedMethods = true
                    }
                }
            }
        }

        if (!modifiedMethods) throw PatchException("Could not find methods to modify")

        // Disable the double speed seek gesture.
        if (!VersionCheckPatch.is_19_17_or_greater) {
            DisableFastForwardLegacyFingerprint.resultOrThrow().let {
                it.mutableMethod.apply {
                    val insertIndex = it.scanResult.patternScanResult!!.endIndex + 1
                    val targetRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                    addInstructions(
                        insertIndex,
                        """
                            invoke-static { v$targetRegister }, $INTEGRATIONS_METHOD_DESCRIPTOR
                            move-result v$targetRegister
                        """
                    )
                }
            }
        } else {
            arrayOf(
                DisableFastForwardGestureFingerprint,
                DisableFastForwardNoticeFingerprint
            ).forEach { it.resultOrThrow().let {
                it.mutableMethod.apply {
                    val targetIndex = it.scanResult.patternScanResult!!.endIndex
                    val targetRegister = getInstruction<OneRegisterInstruction>(targetIndex).registerA

                    addInstructions(
                        targetIndex + 1,
                        """
                            invoke-static { v$targetRegister }, $INTEGRATIONS_METHOD_DESCRIPTOR
                            move-result v$targetRegister
                        """
                    )
                }
            }}
        }
    }
}