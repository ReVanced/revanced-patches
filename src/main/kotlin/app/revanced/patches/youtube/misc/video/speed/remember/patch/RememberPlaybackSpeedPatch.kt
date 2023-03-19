package app.revanced.patches.youtube.misc.video.speed.remember.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.patch.*
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.video.speed.remember.annotation.RememberPlaybackSpeedCompatibility
import app.revanced.patches.youtube.misc.video.speed.remember.fingerprint.InitializePlaybackSpeedValuesFingerprint
import app.revanced.patches.youtube.misc.video.speed.remember.fingerprint.OnPlaybackSpeedItemClickFingerprint
import app.revanced.patches.youtube.misc.video.videoid.patch.VideoIdPatch
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.FiveRegisterInstruction
import org.jf.dexlib2.iface.instruction.Instruction
import org.jf.dexlib2.iface.instruction.ReferenceInstruction

@Patch
@Name("remember-playback-speed")
@Description("Adds the ability to remember the playback speed you chose in the video playback speed flyout.")
@DependsOn([IntegrationsPatch::class, SettingsPatch::class, VideoIdPatch::class])
@RememberPlaybackSpeedCompatibility
@Version("0.0.1")
class RememberPlaybackSpeedPatch : BytecodePatch(
    listOf(
        OnPlaybackSpeedItemClickFingerprint,
        InitializePlaybackSpeedValuesFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            SwitchPreference(
                "revanced_remember_playback_speed_last_selected",
                StringResource(
                    "revanced_remember_playback_speed_last_selected_title",
                    "Remember playback speed changes"
                ),
                true,
                StringResource(
                    "revanced_remember_playback_speed_last_selected_summary_on",
                    "Playback speed changes apply to all videos"
                ),
                StringResource(
                    "revanced_remember_playback_speed_last_selected_summary_off",
                    "Playback speed changes only apply to the current video"
                )
            )
        )

        VideoIdPatch.injectCall("${INTEGRATIONS_CLASS_DESCRIPTOR}->newVideoLoaded(Ljava/lang/String;)V")

        /*
         * The following code works by hooking the method which is called when the user selects a playback speed
         * to remember the last selected playback speed.
         *
         * It also hooks the method which is called when the playback speeds are initialized.
         * Conveniently, at this point the playback speed is set to the remembered playback speed.
         */


        // Set the remembered playback speed.
        InitializePlaybackSpeedValuesFingerprint.result?.apply {
            // Infer everything necessary for calling the method setPlaybackSpeed().
            val instructions = OnPlaybackSpeedItemClickFingerprint.result!!.mutableMethod.implementation!!.instructions
            fun getReference(offset: Int = 0, opcode: Opcode) =
                instructions[instructions.indexOfFirst { it.opcode == opcode } + offset].reference

            val setPlaybackSpeedContainerClassFieldReference =
                getReference(-1, Opcode.IF_EQZ)

            val setPlaybackSpeedClassFieldReference =
                getReference(1, Opcode.IGET)

            val setPlaybackSpeedMethodReference =
                getReference(2, Opcode.IGET)

            val onItemClickListenerClassFieldReference = mutableMethod.instruction(0).reference

            // Registers are not used at index 0, so they can be freely used.
            mutableMethod.addInstructions(
                0,
                """
                    invoke-static { }, $INTEGRATIONS_CLASS_DESCRIPTOR->getCurrentPlaybackSpeed()F
                    move-result v0
                    
                    # Check if the playback speed is not 1.0x.
                    const/high16 v1, 0x3f800000  # 1.0f
                    cmpg-float v1, v0, v1
                    if-eqz v1, :do_not_override
    
                    # Get the instance of the class which has the container class field below.
                    iget-object v1, p0, $onItemClickListenerClassFieldReference

                    # Get the container class field.
                    iget-object v1, v1, $setPlaybackSpeedContainerClassFieldReference 
                    
                    # Get the field from its class.
                    iget-object v2, v1, $setPlaybackSpeedClassFieldReference
                    
                    # Invoke setPlaybackSpeed on that class.
                    invoke-virtual {v2, v0}, $setPlaybackSpeedMethodReference
                """.trimIndent(),
                listOf(ExternalLabel("do_not_override", mutableMethod.instruction(0)))
            )
        } ?: return InitializePlaybackSpeedValuesFingerprint.toErrorResult()

        // Remember the selected playback speed.
        OnPlaybackSpeedItemClickFingerprint.result?.apply {
            val setPlaybackSpeedIndex = scanResult.patternScanResult!!.startIndex - 3

            val selectedPlaybackSpeedRegister =
                (mutableMethod.instruction(setPlaybackSpeedIndex) as FiveRegisterInstruction).registerD

            mutableMethod.addInstruction(
                setPlaybackSpeedIndex,
                "invoke-static { v$selectedPlaybackSpeedRegister }, $INTEGRATIONS_CLASS_DESCRIPTOR->setPlaybackSpeed(F)V"
            )
        } ?: return OnPlaybackSpeedItemClickFingerprint.toErrorResult()

        return PatchResultSuccess()
    }

    private companion object {
        const val INTEGRATIONS_CLASS_DESCRIPTOR =
            "Lapp/revanced/integrations/patches/playback/speed/RememberPlaybackSpeedPatch;"

        val Instruction.reference get() = (this as ReferenceInstruction).reference.toString()
    }
}