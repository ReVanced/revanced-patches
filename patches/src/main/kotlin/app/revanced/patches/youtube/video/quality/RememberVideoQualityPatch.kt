package app.revanced.patches.youtube.video.quality

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playertype.playerTypeHookPatch
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.shared.videoQualityChangedFingerprint
import app.revanced.patches.youtube.video.information.onCreateHook
import app.revanced.patches.youtube.video.information.videoInformationPatch
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/playback/quality/RememberVideoQualityPatch;"
private const val EXTENSION_VIDEO_QUALITY_MENU_INTERFACE =
    "Lapp/revanced/extension/youtube/patches/playback/quality/RememberVideoQualityPatch\$VideoQualityMenuInterface;"

val rememberVideoQualityPatch = bytecodePatch {
    dependsOn(
        sharedExtensionPatch,
        videoInformationPatch,
        playerTypeHookPatch,
        settingsPatch,
        addResourcesPatch,
    )

    execute {
        addResources("youtube", "video.quality.rememberVideoQualityPatch")

        settingsMenuVideoQualityGroup.addAll(listOf(
            ListPreference(
                key = "revanced_video_quality_default_mobile",
                entriesKey = "revanced_video_quality_default_entries",
                entryValuesKey = "revanced_video_quality_default_entry_values"
            ),
            ListPreference(
                key = "revanced_video_quality_default_wifi",
                entriesKey = "revanced_video_quality_default_entries",
                entryValuesKey = "revanced_video_quality_default_entry_values"
            ),
            SwitchPreference("revanced_remember_video_quality_last_selected"),

            ListPreference(
                key = "revanced_shorts_quality_default_mobile",
                entriesKey = "revanced_shorts_quality_default_entries",
                entryValuesKey = "revanced_shorts_quality_default_entry_values",
            ),
            ListPreference(
                key = "revanced_shorts_quality_default_wifi",
                entriesKey = "revanced_shorts_quality_default_entries",
                entryValuesKey = "revanced_shorts_quality_default_entry_values"
            ),
            SwitchPreference("revanced_remember_shorts_quality_last_selected"),
            SwitchPreference("revanced_remember_video_quality_last_selected_toast")
        ))

        onCreateHook(EXTENSION_CLASS_DESCRIPTOR, "newVideoStarted")

        videoQualityFingerprint.let {
            // Fix bad data used by YouTube.
            it.method.addInstructions(
                0,
                """
                    invoke-static { p2, p1 }, $EXTENSION_CLASS_DESCRIPTOR->fixVideoQualityResolution(Ljava/lang/String;I)I    
                    move-result p1
                """
            )

            // Add methods to access obfuscated quality fields.
            it.classDef.apply {
                methods.add(
                    ImmutableMethod(
                        type,
                        "patch_getQualityName",
                        listOf(),
                        "Ljava/lang/String;",
                        AccessFlags.PUBLIC.value or AccessFlags.FINAL.value,
                        null,
                        null,
                        MutableMethodImplementation(2),
                    ).toMutable().apply {
                        // Only one string field.
                        val qualityNameField = fields.single { field ->
                            field.type == "Ljava/lang/String;"
                        }

                        addInstructions(
                            0,
                            """
                                iget-object v0, p0, $qualityNameField
                                return-object v0
                            """
                        )
                    }
                )

                methods.add(
                    ImmutableMethod(
                        type,
                        "patch_getResolution",
                        listOf(),
                        "I",
                        AccessFlags.PUBLIC.value or AccessFlags.FINAL.value,
                        null,
                        null,
                        MutableMethodImplementation(2),
                    ).toMutable().apply {
                        val resolutionField = fields.single { field ->
                            field.type == "I"
                        }

                        addInstructions(
                            0,
                            """
                                iget v0, p0, $resolutionField
                                return v0
                            """
                        )
                    }
                )
            }
        }

        // Inject a call to set the remembered quality once a video loads.
        setVideoQualityFingerprint.match(
            videoQualitySetterFingerprint.originalClassDef
        ).let { match ->
            // This instruction refers to the field with the type that contains the setQuality method.
            val instructions = match.method.implementation!!.instructions
            val onItemClickListenerClassReference =
                (instructions.elementAt(0) as ReferenceInstruction).reference
            val setQualityFieldReference =
                ((instructions.elementAt(1) as ReferenceInstruction).reference) as FieldReference

            proxy(
                classes.find { classDef ->
                    classDef.type == setQualityFieldReference.type
                }!!
            ).mutableClass.apply {
                // Add interface and helper methods to allow extension code to call obfuscated methods.
                interfaces.add(EXTENSION_VIDEO_QUALITY_MENU_INTERFACE)

                methods.add(
                    ImmutableMethod(
                        type,
                        "patch_setQuality",
                        listOf(
                            ImmutableMethodParameter(YOUTUBE_VIDEO_QUALITY_CLASS_TYPE, null, null)
                        ),
                        "V",
                        AccessFlags.PUBLIC.value or AccessFlags.FINAL.value,
                        null,
                        null,
                        MutableMethodImplementation(2),
                    ).toMutable().apply {
                        val setQualityMenuIndexMethod = methods.single { method ->
                            method.parameterTypes.firstOrNull() == YOUTUBE_VIDEO_QUALITY_CLASS_TYPE
                        }

                        addInstructions(
                            0,
                            """
                                invoke-virtual { p0, p1 }, $setQualityMenuIndexMethod
                                return-void
                            """
                        )
                    }
                )
            }

            videoQualitySetterFingerprint.method.addInstructions(
                0,
                """
                    # Get object instance to invoke setQuality method.
                    iget-object v0, p0, $onItemClickListenerClassReference
                    iget-object v0, v0, $setQualityFieldReference
                    
                    invoke-static { p1, v0, p2 }, $EXTENSION_CLASS_DESCRIPTOR->setVideoQuality([$YOUTUBE_VIDEO_QUALITY_CLASS_TYPE${EXTENSION_VIDEO_QUALITY_MENU_INTERFACE}I)I
                    move-result p2
                """
            )
        }

        // Inject a call to remember the selected quality for Shorts.
        videoQualityItemOnClickFingerprint.match(
            videoQualityItemOnClickParentFingerprint.classDef
        ).method.addInstruction(
            0,
            "invoke-static { p3 }, $EXTENSION_CLASS_DESCRIPTOR->userChangedShortsQuality(I)V"
        )

        // Inject a call to remember the user selected quality for regular videos.
        videoQualityChangedFingerprint.let {
            it.method.apply {
                val index = it.patternMatch!!.startIndex
                val register = getInstruction<TwoRegisterInstruction>(index).registerA

                addInstruction(
                    index + 1,
                    "invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->userChangedQuality(I)V",
                )
            }
        }
    }
}
