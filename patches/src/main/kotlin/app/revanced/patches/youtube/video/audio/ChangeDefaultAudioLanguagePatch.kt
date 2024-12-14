package app.revanced.patches.youtube.video.audio

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/ChangeDefaultAudioLanguagePatch;"

@Suppress("unused")
val changeDefaultAudioLanguagePatch = bytecodePatch(
    name = "Change default audio track",
    description = "Adds an option to set a video default audio language .",
) {
    dependsOn(
        sharedExtensionPatch,
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
        ),
    )

    execute {
        addResources("youtube", "video.audio.changeDefaultAudioTrackPatch")

        PreferenceScreen.VIDEO.addPreferences(
            ListPreference(
                "revanced_audio_default_language",
                summaryKey = null
            )
        )

        fun Method.firstFormatStreamingModelCall(
            returnType: String = "Ljava/lang/String;"
        ): MutableMethod {
            val audioTrackIdIndex = indexOfFirstInstructionOrThrow {
                val reference = getReference<MethodReference>()
                reference?.definingClass == "Lcom/google/android/libraries/youtube/innertube/model/media/FormatStreamModel;"
                        && reference.returnType == returnType
            }

            return navigate(this).to(audioTrackIdIndex).stop()
        }

        // Accessor methods of FormatStreamModel have no string constants and
        // opcodes are identical to other methods in the same class,
        // so must walk from another class that use the methods.
        val isDefaultMethod = streamingModelBuilderFingerprint.originalMethod.firstFormatStreamingModelCall("Z")
        val audioTrackIdMethod = menuItemAudioTrackFingerprint.originalMethod.firstFormatStreamingModelCall()
        val audioTrackDisplayNameMethod = audioStreamingTypeSelector.originalMethod.firstFormatStreamingModelCall()
        val formatStreamModelClass = proxy(classes.first {
            it.type == audioTrackIdMethod.definingClass
        }).mutableClass

        formatStreamModelClass.apply {
            // Add a helper method because the isDefaultAudioTrack() has only 2 registers and 3 are needed.
            val helperMethodClass = type
            val helperMethodName = "extensions_isDefaultAudioTrack"
            val helperMethod = ImmutableMethod(
                helperMethodClass,
                helperMethodName,
                listOf(ImmutableMethodParameter("Z", null, null)),
                "Z",
                AccessFlags.PRIVATE.value,
                null,
                null,
                MutableMethodImplementation(4),
            ).toMutable().apply {
                // This is the equivalent of
                //   String featureName = feature.toString()
                //   <inject more instructions here later>
                //   return null
                addInstructions(
                    0,
                    """
                        invoke-virtual { p0 }, $audioTrackIdMethod
                        move-result-object v0
                        
                        invoke-virtual { p0 }, $audioTrackDisplayNameMethod
                        move-result-object v1
    
                        invoke-static { p1, v0, v1 }, $EXTENSION_CLASS_DESCRIPTOR->setAudioStreamAsDefault(ZLjava/lang/String;Ljava/lang/String;)Z
                        move-result v0
                        
                        return v0
                    """
                )
            }
            methods.add(helperMethod)

            // Modify isDefaultAudioTrack() to call extension helper method.
            isDefaultMethod.apply {
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
