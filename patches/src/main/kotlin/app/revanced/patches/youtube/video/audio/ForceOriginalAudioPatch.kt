package app.revanced.patches.youtube.video.audio

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableField.Companion.toMutable
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.is_20_07_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.shared.mainActivityOnCreateFingerprint
import app.revanced.util.findMethodFromToString
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.insertLiteralOverride
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.immutable.ImmutableField
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/ForceOriginalAudioPatch;"

@Suppress("unused")
val forceOriginalAudioPatch = bytecodePatch(
    name = "Force original audio",
    description = "Adds an option to always use the original audio track.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
        versionCheckPatch
    )

    compatibleWith(
        "com.google.android.youtube"(
            "19.34.42",
            "20.07.39",
            "20.13.41",
            "20.14.43",
        )
    )

    execute {
        addResources("youtube", "video.audio.forceOriginalAudioPatch")

        PreferenceScreen.VIDEO.addPreferences(
            SwitchPreference(
                key = "revanced_force_original_audio",
                tag = "app.revanced.extension.youtube.settings.preference.ForceOriginalAudioSwitchPreference"
            )
        )

        mainActivityOnCreateFingerprint.method.addInstruction(
            0,
            "invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->setPreferredLanguage()V"
        )

        // Disable feature flag that ignores the default track flag
        // and instead overrides to the user region language.
        if (is_20_07_or_greater) {
            selectAudioStreamFingerprint.method.insertLiteralOverride(
                AUDIO_STREAM_IGNORE_DEFAULT_FEATURE_FLAG,
                "$EXTENSION_CLASS_DESCRIPTOR->ignoreDefaultAudioStream(Z)Z"
            )
        }

        formatStreamModelToStringFingerprint.let {
            val isDefaultAudioTrackMethod = it.originalMethod.findMethodFromToString("isDefaultAudioTrack=")
            val audioTrackDisplayNameMethod = it.originalMethod.findMethodFromToString("audioTrackDisplayName=")
            val audioTrackIdMethod = it.originalMethod.findMethodFromToString("audioTrackId=")

            it.classDef.apply {
                // Add a new field to store the override.
                val helperFieldName = "patch_isDefaultAudioTrackOverride"
                fields.add(
                    ImmutableField(
                        type,
                        helperFieldName,
                        "Ljava/lang/Boolean;",
                        // Boolean is a 100% immutable class (all fields are final)
                        // and safe to write to a shared field without volatile/synchronization,
                        // but without volatile the field can show stale data
                        // and the same field is calculated more than once by different threads.
                        AccessFlags.PRIVATE.value or AccessFlags.VOLATILE.value,
                        null,
                        null,
                        null
                    ).toMutable()
                )

                // Add a helper method because the isDefaultAudioTrack() has only 2 registers and 3 are needed.
                val helperMethodClass = type
                val helperMethodName = "patch_isDefaultAudioTrack"
                val helperMethod = ImmutableMethod(
                    helperMethodClass,
                    helperMethodName,
                    listOf(ImmutableMethodParameter("Z", null, null)),
                    "Z",
                    AccessFlags.PRIVATE.value,
                    null,
                    null,
                    MutableMethodImplementation(6),
                ).toMutable().apply {
                    addInstructionsWithLabels(
                        0,
                        """
                            iget-object v0, p0, $helperMethodClass->$helperFieldName:Ljava/lang/Boolean;
                            if-eqz v0, :call_extension            
                            invoke-virtual { v0 }, Ljava/lang/Boolean;->booleanValue()Z
                            move-result v3
                            return v3
                            
                            :call_extension
                            invoke-virtual { p0 }, $audioTrackIdMethod
                            move-result-object v1
                            
                            invoke-virtual { p0 }, $audioTrackDisplayNameMethod
                            move-result-object v2
        
                            invoke-static { p1, v1, v2 }, $EXTENSION_CLASS_DESCRIPTOR->isDefaultAudioStream(ZLjava/lang/String;Ljava/lang/String;)Z
                            move-result v3
                            
                            invoke-static { v3 }, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
                            move-result-object v0
                            iput-object v0, p0, $helperMethodClass->$helperFieldName:Ljava/lang/Boolean;
                            return v3
                        """
                    )
                }
                methods.add(helperMethod)

                // Modify isDefaultAudioTrack() to call extension helper method.
                isDefaultAudioTrackMethod.apply {
                    val index = indexOfFirstInstructionOrThrow(Opcode.RETURN)
                    val register = getInstruction<OneRegisterInstruction>(index).registerA

                    addInstructions(
                        index,
                        """
                            invoke-direct { p0, v$register }, $helperMethodClass->$helperMethodName(Z)Z
                            move-result v$register
                        """
                    )
                }
            }
        }
    }
}
