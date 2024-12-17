package app.revanced.patches.youtube.layout.shortsbypass

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
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

        playbackStartDescriptorFingerprint.method.addInstructions(
            0,
            """
                move-object/from16 v0, p1
                invoke-virtual { v0 }, Lcom/google/android/libraries/youtube/player/model/PlaybackStartDescriptor;->$playbackStartVideoIdMethodName()Ljava/lang/String;
                move-result-object v0
                invoke-static { v0 }, $EXTENSION_CLASS_DESCRIPTOR->openShorts(Ljava/lang/String;)Z
                move-result v0
               
                if-eqz v0, :disabled
                return-void
                :disabled
                nop
            """
        )
    }
}
