package app.revanced.patches.youtube.video.speed.remember

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.patches.youtube.video.information.VideoInformationPatch
import app.revanced.patches.youtube.video.speed.custom.CustomPlaybackSpeedPatch
import app.revanced.patches.youtube.video.speed.remember.fingerprint.InitializePlaybackSpeedValuesFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

@Patch(
    dependencies = [
        IntegrationsPatch::class,
        SettingsPatch::class,
        VideoInformationPatch::class,
        CustomPlaybackSpeedPatch::class,
        AddResourcesPatch::class
    ]
)
object RememberPlaybackSpeedPatch : BytecodePatch(
    setOf(InitializePlaybackSpeedValuesFingerprint)
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/playback/speed/RememberPlaybackSpeedPatch;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.VIDEO.addPreferences(
            SwitchPreference("revanced_remember_playback_speed_last_selected"),
            ListPreference(
                key = "revanced_playback_speed_default",
                summaryKey = null,
                // Entries and values are set by Integrations code based on the actual speeds available.
                entriesKey = null,
                entryValuesKey = null
            )
        )

        VideoInformationPatch.onCreateHook(INTEGRATIONS_CLASS_DESCRIPTOR, "newVideoStarted")
        VideoInformationPatch.userSelectedPlaybackSpeedHook(
            INTEGRATIONS_CLASS_DESCRIPTOR, "userSelectedPlaybackSpeed"
        )

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
}
