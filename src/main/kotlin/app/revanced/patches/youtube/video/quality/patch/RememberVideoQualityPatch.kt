package app.revanced.patches.youtube.video.quality.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.shared.settings.preference.impl.ArrayResource
import app.revanced.patches.shared.settings.preference.impl.ListPreference
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.video.information.patch.VideoInformationPatch
import app.revanced.patches.youtube.video.quality.annotations.RememberVideoQualityCompatibility
import app.revanced.patches.youtube.video.quality.fingerprints.NewVideoQualityChangedFingerprint
import app.revanced.patches.youtube.video.quality.fingerprints.SetQualityByIndexMethodClassFieldReferenceFingerprint
import app.revanced.patches.youtube.video.quality.fingerprints.VideoQualityItemOnClickParentFingerprint
import app.revanced.patches.youtube.video.quality.fingerprints.VideoQualitySetterFingerprint
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

@Patch
@DependsOn([IntegrationsPatch::class, VideoInformationPatch::class, SettingsPatch::class])
@Name("Remember video quality")
@Description("Adds the ability to remember the video quality you chose in the video quality flyout.")
@RememberVideoQualityCompatibility
class RememberVideoQualityPatch : BytecodePatch(
    listOf(
        VideoQualitySetterFingerprint,
        VideoQualityItemOnClickParentFingerprint,
        NewVideoQualityChangedFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        // This is bloated as each value has it's own String key/value
        // ideally the entries would be raw values (and not a key to a String resource)
        val entries = listOf(
            StringResource("revanced_video_quality_default_entry_1", "Automatic quality"),
            StringResource("revanced_video_quality_default_entry_2", "2160p"),
            StringResource("revanced_video_quality_default_entry_3", "1440p"),
            StringResource("revanced_video_quality_default_entry_4", "1080p"),
            StringResource("revanced_video_quality_default_entry_5", "720p"),
            StringResource("revanced_video_quality_default_entry_6", "480p"),
            StringResource("revanced_video_quality_default_entry_7", "360p"),
            StringResource("revanced_video_quality_default_entry_8", "240p"),
            StringResource("revanced_video_quality_default_entry_9", "144p"),
        )
        val entryValues = listOf(
            StringResource("revanced_video_quality_default_entry_value_1", "-2"),
            StringResource("revanced_video_quality_default_entry_value_2", "2160"),
            StringResource("revanced_video_quality_default_entry_value_3", "1440"),
            StringResource("revanced_video_quality_default_entry_value_4", "1080"),
            StringResource("revanced_video_quality_default_entry_value_5", "720"),
            StringResource("revanced_video_quality_default_entry_value_6", "480"),
            StringResource("revanced_video_quality_default_entry_value_7", "360"),
            StringResource("revanced_video_quality_default_entry_value_8", "240"),
            StringResource("revanced_video_quality_default_entry_value_9", "144"),
        )

        SettingsPatch.PreferenceScreen.VIDEO.addPreferences(
            SwitchPreference(
                "revanced_remember_video_quality_last_selected",
                StringResource(
                    "revanced_remember_video_quality_last_selected_title",
                    "Remember video quality changes"
                ),
                StringResource(
                    "revanced_remember_video_quality_last_selected_summary_on",
                    "Quality changes apply to all videos"
                ),
                StringResource(
                    "revanced_remember_video_quality_last_selected_summary_off",
                    "Quality changes only apply to the current video"
                )
            ),
            ListPreference(
                "revanced_video_quality_default_wifi",
                StringResource(
                    "revanced_video_quality_default_wifi_title",
                    "Default video quality on Wi-Fi network"
                ),
                ArrayResource("revanced_video_quality_default_wifi_entry", entries),
                ArrayResource("revanced_video_quality_default_wifi_entry_values", entryValues)
                // default value and summary are set by integrations after loading
            ),
            ListPreference(
                "revanced_video_quality_default_mobile",
                StringResource(
                    "revanced_video_quality_default_mobile_title",
                    "Default video quality on mobile network"
                ),
                ArrayResource("revanced_video_quality_default_mobile_entries", entries),
                ArrayResource("revanced_video_quality_default_mobile_values", entryValues)
            )
        )

        /*
         * The following code works by hooking the method which is called when the user selects a video quality
         * to remember the last selected video quality.
         *
         * It also hooks the method which is called when the video quality to set is determined.
         * Conveniently, at this point the video quality is overridden to the remembered playback speed.
         */

        VideoInformationPatch.onCreateHook(INTEGRATIONS_CLASS_DESCRIPTOR, "newVideoStarted")


        // Inject a call to set the remembered quality once a video loads.
        VideoQualitySetterFingerprint.result?.also {
            if (!SetQualityByIndexMethodClassFieldReferenceFingerprint.resolve(context, it.classDef))
                throw PatchException("Could not resolve fingerprint to find setQualityByIndex method")
        }?.let {
            // This instruction refers to the field with the type that contains the setQualityByIndex method.
            val instructions = SetQualityByIndexMethodClassFieldReferenceFingerprint.result!!
                .method.implementation!!.instructions

            val getOnItemClickListenerClassReference =
                (instructions.elementAt(0) as ReferenceInstruction).reference
            val getSetQualityByIndexMethodClassFieldReference =
                (instructions.elementAt(1) as ReferenceInstruction).reference

            val setQualityByIndexMethodClassFieldReference =
                getSetQualityByIndexMethodClassFieldReference as FieldReference

            val setQualityByIndexMethodClass = context.classes
                .find { classDef -> classDef.type == setQualityByIndexMethodClassFieldReference.type }!!

            // Get the name of the setQualityByIndex method.
            val setQualityByIndexMethod = setQualityByIndexMethodClass.methods
                .find { method -> method.parameterTypes.first() == "I" }
                ?: throw PatchException("Could not find setQualityByIndex method")

            it.mutableMethod.addInstructions(
                0,
                """
                    # Get the object instance to invoke the setQualityByIndex method on.
                    iget-object v0, p0, $getOnItemClickListenerClassReference
                    iget-object v0, v0, $getSetQualityByIndexMethodClassFieldReference
                    
                    # Get the method name.
                    const-string v1, "${setQualityByIndexMethod.name}"
                    
                    # Set the quality.
                    # The first parameter is the array list of video qualities.
                    # The second parameter is the index of the selected quality.
                    # The register v0 stores the object instance to invoke the setQualityByIndex method on.
                    # The register v1 stores the name of the setQualityByIndex method.
                    invoke-static {p1, p2, v0, v1}, $INTEGRATIONS_CLASS_DESCRIPTOR->setVideoQuality([Ljava/lang/Object;ILjava/lang/Object;Ljava/lang/String;)I
                    move-result p2
                """,
            )
        } ?: throw VideoQualitySetterFingerprint.exception


        // Inject a call to remember the selected quality.
        VideoQualityItemOnClickParentFingerprint.result?.let {
            val onItemClickMethod = it.mutableClass.methods.find { method -> method.name == "onItemClick" }

            onItemClickMethod?.apply {
                val listItemIndexParameter = 3

                addInstruction(
                    0,
                    "invoke-static {p$listItemIndexParameter}, $INTEGRATIONS_CLASS_DESCRIPTOR->userChangedQuality(I)V"
                )
            } ?: throw PatchException("Failed to find onItemClick method")
        } ?: throw VideoQualityItemOnClickParentFingerprint.exception


        // Remember video quality if not using old layout menu.
        NewVideoQualityChangedFingerprint.result?.apply {
            mutableMethod.apply {
                val index = scanResult.patternScanResult!!.startIndex
                val qualityRegister = getInstruction<TwoRegisterInstruction>(index).registerA

                addInstruction(
                    index + 1,
                    "invoke-static {v$qualityRegister}, $INTEGRATIONS_CLASS_DESCRIPTOR->userChangedQualityInNewFlyout(I)V"
                )
            }
        } ?: throw NewVideoQualityChangedFingerprint.exception

    }

    private companion object {
        const val INTEGRATIONS_CLASS_DESCRIPTOR =
            "Lapp/revanced/integrations/patches/playback/quality/RememberVideoQualityPatch;"
    }
}
