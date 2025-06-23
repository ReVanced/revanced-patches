package app.revanced.patches.youtube.video.speed.remember

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.video.information.*
import app.revanced.patches.youtube.video.speed.custom.customPlaybackSpeedPatch
import app.revanced.patches.youtube.video.speed.settingsMenuVideoSpeedGroup
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/playback/speed/RememberPlaybackSpeedPatch;"

internal val rememberPlaybackSpeedPatch = bytecodePatch {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
        videoInformationPatch,
        customPlaybackSpeedPatch
    )

    execute {
        addResources("youtube", "video.speed.remember.rememberPlaybackSpeedPatch")

        settingsMenuVideoSpeedGroup.addAll(
            listOf(
                ListPreference(
                    key = "revanced_playback_speed_default",
                    // Entries and values are set by the extension code based on the actual speeds available.
                    entriesKey = null,
                    entryValuesKey = null,
                    tag = "app.revanced.extension.youtube.settings.preference.CustomVideoSpeedListPreference"
                ),
                SwitchPreference("revanced_remember_playback_speed_last_selected"),
                SwitchPreference("revanced_remember_playback_speed_last_selected_toast")
            )
        )

        onCreateHook(EXTENSION_CLASS_DESCRIPTOR, "newVideoStarted")

        userSelectedPlaybackSpeedHook(
            EXTENSION_CLASS_DESCRIPTOR,
            "userSelectedPlaybackSpeed",
        )

        /*
         * Hook the code that is called when the playback speeds are initialized, and sets the playback speed
         */
        initializePlaybackSpeedValuesFingerprint.method.apply {
            // Infer everything necessary for calling the method setPlaybackSpeed().
            val onItemClickListenerClassFieldReference = getInstruction<ReferenceInstruction>(0).reference

            // Registers are not used at index 0, so they can be freely used.
            addInstructionsWithLabels(
                0,
                """
                    invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->getPlaybackSpeedOverride()F
                    move-result v0
                    
                    # Check if the playback speed is not auto (-2.0f)
                    const/4 v1, 0x0
                    cmpg-float v1, v0, v1
                    if-lez v1, :do_not_override
    
                    # Get the instance of the class which has the container class field below.
                    iget-object v1, p0, $onItemClickListenerClassFieldReference

                    # Get the container class field.
                    iget-object v1, v1, $setPlaybackSpeedContainerClassFieldReference  
                    
                    # Get the field from its class.
                    iget-object v2, v1, $setPlaybackSpeedClassFieldReference
                    
                    # Invoke setPlaybackSpeed on that class.
                    invoke-virtual {v2, v0}, $setPlaybackSpeedMethodReference
                """,
                ExternalLabel("do_not_override", getInstruction(0)),
            )
        }
    }
}
