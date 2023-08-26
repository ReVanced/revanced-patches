package app.revanced.patches.youtube.video.speed.remember.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.shared.settings.preference.impl.ArrayResource
import app.revanced.patches.shared.settings.preference.impl.ListPreference
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.video.information.patch.VideoInformationPatch
import app.revanced.patches.youtube.video.speed.custom.patch.CustomPlaybackSpeedPatch
import app.revanced.patches.youtube.video.speed.remember.fingerprint.InitializePlaybackSpeedValuesFingerprint
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

@Name("Remember playback speed")
@Description("Adds the ability to remember the playback speed you chose in the playback speed flyout.")
@DependsOn([IntegrationsPatch::class, SettingsPatch::class, VideoInformationPatch::class, CustomPlaybackSpeedPatch::class])
class RememberPlaybackSpeedPatch : BytecodePatch(
    listOf(
        InitializePlaybackSpeedValuesFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        SettingsPatch.PreferenceScreen.VIDEO.addPreferences(
            SwitchPreference(
                "revanced_remember_playback_speed_last_selected",
                StringResource(
                    "revanced_remember_playback_speed_last_selected_title",
                    "Remember playback speed changes"
                ),
                StringResource(
                    "revanced_remember_playback_speed_last_selected_summary_on",
                    "Playback speed changes apply to all videos"
                ),
                StringResource(
                    "revanced_remember_playback_speed_last_selected_summary_off",
                    "Playback speed changes only apply to the current video"
                )
            ),
            ListPreference(
                "revanced_playback_speed_default",
                StringResource(
                    "revanced_playback_speed_default_title",
                    "Default playback speed"
                ),
                // Dummy data:
                // Entries and values are set by Integrations code based on the actual speeds available,
                // and the values set here are ignored and do nothing.
                ArrayResource(
                    "revanced_playback_speed_default_entries",
                    listOf(StringResource("revanced_playback_speed_default_entries", "1.0x"))
                ),
                ArrayResource(
                    "revanced_playback_speed_default_entry_values",
                    listOf(StringResource("revanced_playback_speed_default_entry_value", "1.0"))
                )
            )
        )

        VideoInformationPatch.onCreateHook(INTEGRATIONS_CLASS_DESCRIPTOR, "newVideoStarted")
        VideoInformationPatch.userSelectedPlaybackSpeedHook(
            INTEGRATIONS_CLASS_DESCRIPTOR, "userSelectedPlaybackSpeed")

        /*
         * Hook the code that is called when the playback speeds are initialized, and sets the playback speed
         */
        InitializePlaybackSpeedValuesFingerprint.result?.apply {
            // Infer everything necessary for calling the method setPlaybackSpeed().
            val onItemClickListenerClassFieldReference =
                mutableMethod.getInstruction<ReferenceInstruction>(0).reference

            // Registers are not used at index 0, so they can be freely used.
            mutableMethod.addInstructionsWithLabels(
                0,
                """
                    invoke-static { }, $INTEGRATIONS_CLASS_DESCRIPTOR->getPlaybackSpeedOverride()F
                    move-result v0
                    
                    # Check if the playback speed is not 1.0x.
                    const/high16 v1, 0x3f800000  # 1.0f
                    cmpg-float v1, v0, v1
                    if-eqz v1, :do_not_override
    
                    # Get the instance of the class which has the container class field below.
                    iget-object v1, p0, $onItemClickListenerClassFieldReference

                    # Get the container class field.
                    iget-object v1, v1, ${VideoInformationPatch.setPlaybackSpeedContainerClassFieldReference}  
                    
                    # Get the field from its class.
                    iget-object v2, v1, ${VideoInformationPatch.setPlaybackSpeedClassFieldReference}
                    
                    # Invoke setPlaybackSpeed on that class.
                    invoke-virtual {v2, v0}, ${VideoInformationPatch.setPlaybackSpeedMethodReference}
                """.trimIndent(),
                ExternalLabel("do_not_override", mutableMethod.getInstruction(0))
            )
        } ?: throw InitializePlaybackSpeedValuesFingerprint.exception
    }

    private companion object {
        const val INTEGRATIONS_CLASS_DESCRIPTOR =
            "Lapp/revanced/integrations/patches/playback/speed/RememberPlaybackSpeedPatch;"
    }
}
