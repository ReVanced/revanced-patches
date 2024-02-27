package app.revanced.patches.youtube.seekbar.append

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.utils.fingerprints.TotalTimeFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.SEEKBAR
import app.revanced.patches.youtube.utils.overridequality.OverrideQualityHookPatch
import app.revanced.patches.youtube.utils.overridespeed.OverrideSpeedHookPatch
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.TotalTime
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.util.exception
import app.revanced.util.getReference
import app.revanced.util.getTargetIndex
import app.revanced.util.getWideLiteralInstructionIndex
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "Append time stamps information",
    description = "Adds an option to add the current video quality or playback speed in brackets next to the current time.",
    dependencies = [
        OverrideQualityHookPatch::class,
        OverrideSpeedHookPatch::class,
        SettingsPatch::class,
        SharedResourceIdPatch::class
    ],
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
object AppendTimeStampInformationPatch : BytecodePatch(
    setOf(TotalTimeFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        TotalTimeFingerprint.result?.let {
            it.mutableMethod.apply {
                val constIndex = getWideLiteralInstructionIndex(TotalTime)
                val charSequenceIndex = getTargetIndex(constIndex, Opcode.MOVE_RESULT_OBJECT)
                val charSequenceRegister = getInstruction<OneRegisterInstruction>(charSequenceIndex).registerA
                val textViewIndex = indexOfFirstInstruction {
                    getReference<MethodReference>()?.name == "getText"
                }
                val textViewRegister =
                    getInstruction<Instruction35c>(textViewIndex).registerC

                addInstructions(
                    textViewIndex, """
                        invoke-static {v$textViewRegister}, $SEEKBAR->setContainerClickListener(Landroid/view/View;)V
                        invoke-static {v$charSequenceRegister}, $SEEKBAR->appendTimeStampInformation(Ljava/lang/String;)Ljava/lang/String;
                        move-result-object v$charSequenceRegister
                        """
                )
            }
        } ?: throw TotalTimeFingerprint.exception

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: SEEKBAR_SETTINGS",
                "SETTINGS: APPEND_TIME_STAMP_INFORMATION"
            )
        )

        SettingsPatch.updatePatchStatus("Append time stamps information")

    }
}
