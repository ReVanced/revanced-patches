package app.revanced.patches.youtube.video.quality

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.shared.newVideoQualityChangedFingerprint
import app.revanced.patches.youtube.video.information.onCreateHook
import app.revanced.patches.youtube.video.information.videoInformationPatch
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/playback/quality/RememberVideoQualityPatch;"

val rememberVideoQualityPatch = bytecodePatch(
    name = "Remember video quality",
    description = "Adds an option to remember the last video quality selected.",
) {
    dependsOn(
        sharedExtensionPatch,
        videoInformationPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "18.38.44",
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
        addResources("youtube", "video.quality.rememberVideoQualityPatch")

        PreferenceScreen.VIDEO.addPreferences(
            SwitchPreference("revanced_remember_video_quality_last_selected"),
            ListPreference(
                key = "revanced_video_quality_default_wifi",
                summaryKey = null,
                entriesKey = "revanced_video_quality_default_entries",
                entryValuesKey = "revanced_video_quality_default_entry_values",
            ),
            ListPreference(
                key = "revanced_video_quality_default_mobile",
                summaryKey = null,
                entriesKey = "revanced_video_quality_default_entries",
                entryValuesKey = "revanced_video_quality_default_entry_values",
            ),
        )

        /*
         * The following code works by hooking the method which is called when the user selects a video quality
         * to remember the last selected video quality.
         *
         * It also hooks the method which is called when the video quality to set is determined.
         * Conveniently, at this point the video quality is overridden to the remembered playback speed.
         */
        onCreateHook(EXTENSION_CLASS_DESCRIPTOR, "newVideoStarted")

        // Inject a call to set the remembered quality once a video loads.
        setQualityByIndexMethodClassFieldReferenceFingerprint.match(
            videoQualitySetterFingerprint.originalClassDef,
        ).let { match ->
            // This instruction refers to the field with the type that contains the setQualityByIndex method.
            val instructions = match.method.implementation!!.instructions

            val getOnItemClickListenerClassReference =
                (instructions.elementAt(0) as ReferenceInstruction).reference
            val getSetQualityByIndexMethodClassFieldReference =
                (instructions.elementAt(1) as ReferenceInstruction).reference

            val setQualityByIndexMethodClassFieldReference =
                getSetQualityByIndexMethodClassFieldReference as FieldReference

            val setQualityByIndexMethodClass = classes
                .find { classDef -> classDef.type == setQualityByIndexMethodClassFieldReference.type }!!

            // Get the name of the setQualityByIndex method.
            val setQualityByIndexMethod = setQualityByIndexMethodClass.methods
                .find { method -> method.parameterTypes.first() == "I" }
                ?: throw PatchException("Could not find setQualityByIndex method")

            videoQualitySetterFingerprint.method.addInstructions(
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
                    invoke-static { p1, p2, v0, v1 }, $EXTENSION_CLASS_DESCRIPTOR->setVideoQuality([Ljava/lang/Object;ILjava/lang/Object;Ljava/lang/String;)I
                    move-result p2
                """,
            )
        }

        // Inject a call to remember the selected quality.
        videoQualityItemOnClickParentFingerprint.classDef.methods.find { it.name == "onItemClick" }
            ?.apply {
                val listItemIndexParameter = 3

                addInstruction(
                    0,
                    "invoke-static { p$listItemIndexParameter }, " +
                        "$EXTENSION_CLASS_DESCRIPTOR->userChangedQuality(I)V",
                )
            } ?: throw PatchException("Failed to find onItemClick method")

        // Remember video quality if not using old layout menu.
        newVideoQualityChangedFingerprint.method.apply {
            val index = newVideoQualityChangedFingerprint.filterMatches[3].index
            val qualityRegister = getInstruction<TwoRegisterInstruction>(index).registerA

            addInstruction(
                index + 1,
                "invoke-static { v$qualityRegister }, " +
                    "$EXTENSION_CLASS_DESCRIPTOR->userChangedQualityInNewFlyout(I)V",
            )
        }
    }
}
