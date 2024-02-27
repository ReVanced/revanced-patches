package app.revanced.patches.youtube.fullscreen.forcefullscreen

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.youtube.fullscreen.forcefullscreen.fingerprints.ClientSettingEndpointFingerprint
import app.revanced.patches.youtube.fullscreen.forcefullscreen.fingerprints.VideoPortraitParentFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.FULLSCREEN
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.util.exception
import app.revanced.util.getStringInstructionIndex
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Patch(
    name = "Force fullscreen",
    description = "Adds an option to forcefully open videos in fullscreen.",
    dependencies = [SettingsPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.25.40",
                "18.27.36",
                "18.29.38",
                "18.30.37",
                "18.31.40",
                "18.32.39",
                "18.33.40",
                "18.34.38",
                "18.35.36",
                "18.36.39",
                "18.37.36",
                "18.38.44",
                "18.39.41",
                "18.40.34",
                "18.41.39",
                "18.42.41",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.46.45",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39"
            ]
        )
    ]
)
@Suppress("unused")
object ForceFullscreenPatch : BytecodePatch(
    setOf(
        ClientSettingEndpointFingerprint,
        VideoPortraitParentFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        /**
         * Process that hooks Activity for using {Activity.setRequestedOrientation}.
         */
        ClientSettingEndpointFingerprint.result?.let {
            it.mutableMethod.apply {
                val getActivityIndex = getStringInstructionIndex("watch") + 2
                val getActivityReference =
                    getInstruction<ReferenceInstruction>(getActivityIndex).reference
                val classRegister =
                    getInstruction<TwoRegisterInstruction>(getActivityIndex).registerB

                val watchDescriptorMethodIndex =
                    getStringInstructionIndex("start_watch_minimized") - 1
                val watchDescriptorRegister =
                    getInstruction<FiveRegisterInstruction>(watchDescriptorMethodIndex).registerD

                addInstructions(
                    watchDescriptorMethodIndex, """
                        invoke-static {v$watchDescriptorRegister}, $FULLSCREEN->forceFullscreen(Z)Z
                        move-result v$watchDescriptorRegister
                        """
                )

                val insertIndex = getStringInstructionIndex("force_fullscreen")
                val freeRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                addInstructions(
                    insertIndex, """
                        iget-object v$freeRegister, v$classRegister, $getActivityReference
                        check-cast v$freeRegister, Landroid/app/Activity;
                        sput-object v$freeRegister, $FULLSCREEN->watchDescriptorActivity:Landroid/app/Activity;
                        """
                )
            }
        } ?: throw ClientSettingEndpointFingerprint.exception

        /**
         * Don't rotate the screen in vertical video.
         * Add an instruction to check the vertical video.
         */
        VideoPortraitParentFingerprint.result?.let {
            it.mutableMethod.apply {
                val stringIndex =
                    getStringInstructionIndex("Acquiring NetLatencyActionLogger failed. taskId=")
                val invokeIndex = getTargetIndexTo(stringIndex, Opcode.INVOKE_INTERFACE)
                val targetIndex = getTargetIndexTo(invokeIndex, Opcode.CHECK_CAST)
                val targetClass = context
                    .findClass(getInstruction<ReferenceInstruction>(targetIndex).reference.toString())!!
                    .mutableClass

                targetClass.methods.find { method -> method.parameters == listOf("I", "I", "Z") }
                    ?.apply {
                        addInstruction(
                            1,
                            "invoke-static {p1, p2}, $FULLSCREEN->setVideoPortrait(II)V"
                        )
                    } ?: throw PatchException("Could not find targetMethod")
            }
        } ?: throw VideoPortraitParentFingerprint.exception

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: FULLSCREEN_SETTINGS",
                "SETTINGS: FULLSCREEN_EXPERIMENTAL_FLAGS",
                "SETTINGS: FORCE_FULLSCREEN"
            )
        )

        SettingsPatch.updatePatchStatus("Force fullscreen")

    }

    private fun MutableMethod.getTargetIndexTo(
        startIndex: Int,
        opcode: Opcode
    ): Int {
        for (index in startIndex until implementation!!.instructions.size) {
            if (getInstruction(index).opcode != opcode)
                continue

            return index
        }
        throw PatchException("Failed to find target index")
    }
}
