package app.revanced.patches.youtube.layout.shortsbypass

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.is_19_46_or_greater
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/OpenShortsInRegularPlayer;"

val openShortsInRegularPlayer = bytecodePatch(
    name = "Open Shorts in player",
    description = "Adds an option to open Shorts in the regular video player.",
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
        addResources("youtube", "layout.shortsbypass.openShortsInRegularPlayer")

        PreferenceScreen.SHORTS.addPreferences(
            SwitchPreference("revanced_open_shorts_in_regular_player"),
        )

        // Find the obfuscated method name for PlaybackStartDescriptor.videoId()
        val playbackStartVideoIdMethodName = playbackStartFeatureFlagFingerprint.method.let {
            val stringMethodIndex = it.indexOfFirstInstructionOrThrow {
                val reference = getReference<MethodReference>()
                reference?.definingClass == "Lcom/google/android/libraries/youtube/player/model/PlaybackStartDescriptor;"
                        && reference.returnType == "Ljava/lang/String;"
            }

            it.getInstruction<ReferenceInstruction>(stringMethodIndex).getReference<MethodReference>()!!.name
        }

        fun extensionInstructions(descriptorRegister: Int, freeRegister: Int) =
            """
                invoke-virtual { v$descriptorRegister }, Lcom/google/android/libraries/youtube/player/model/PlaybackStartDescriptor;->$playbackStartVideoIdMethodName()Ljava/lang/String;
                move-result-object v$freeRegister
                invoke-static { v$freeRegister }, $EXTENSION_CLASS_DESCRIPTOR->openShort(Ljava/lang/String;)Z
                move-result v$freeRegister
                if-eqz v$freeRegister, :disabled
                return-void
                
                :disabled
                nop
            """

        if (!is_19_46_or_greater) {
            playbackStartDescriptorLegacyFingerprint.method.apply {
                val index = indexOfFirstInstructionOrThrow {
                    getReference<MethodReference>()?.returnType ==
                            "Lcom/google/android/libraries/youtube/player/model/PlaybackStartDescriptor;"
                }
                val freeRegister = getInstruction<FiveRegisterInstruction>(index).registerC
                val descriptorRegister = getInstruction<OneRegisterInstruction>(index + 1).registerA

                addInstructionsWithLabels(
                    index + 2,
                    extensionInstructions(descriptorRegister, freeRegister)
                )
            }

            return@execute
        }

        playbackStartDescriptorFingerprint.method.addInstructionsWithLabels(
            0,
            """
                move-object/from16 v0, p1
                ${extensionInstructions(0, 0)}
            """
        )
    }
}
