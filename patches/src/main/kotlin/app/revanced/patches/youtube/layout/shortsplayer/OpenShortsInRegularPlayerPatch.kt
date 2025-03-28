package app.revanced.patches.youtube.layout.shortsplayer

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.patches.youtube.layout.player.fullscreen.openVideosFullscreenHookPatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.navigation.navigationBarHookPatch
import app.revanced.patches.youtube.misc.playservice.is_19_25_or_greater
import app.revanced.patches.youtube.misc.playservice.is_19_46_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.shared.mainActivityOnCreateFingerprint
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/OpenShortsInRegularPlayerPatch;"

@Suppress("unused")
val openShortsInRegularPlayerPatch = bytecodePatch(
    name = "Open Shorts in regular player",
    description = "Adds options to open Shorts in the regular video player.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
        openVideosFullscreenHookPatch,
        navigationBarHookPatch,
        versionCheckPatch
    )

    compatibleWith(
        "com.google.android.youtube"(
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
            "19.47.53",
            "20.07.39",
        ),
    )

    execute {
        addResources("youtube", "layout.shortsplayer.shortsPlayerTypePatch")

        PreferenceScreen.SHORTS.addPreferences(
            if (is_19_46_or_greater) {
                ListPreference(
                    key = "revanced_shorts_player_type",
                    summaryKey = null,
                )
            } else {
                ListPreference(
                    key = "revanced_shorts_player_type",
                    summaryKey = null,
                    entriesKey = "revanced_shorts_player_type_legacy_entries",
                    entryValuesKey = "revanced_shorts_player_type_legacy_entry_values"
                )
            }
        )

        // Activity is used as the context to launch an Intent.
        mainActivityOnCreateFingerprint.method.addInstruction(
            1,
            "invoke-static/range { p0 .. p0 }, ${EXTENSION_CLASS_DESCRIPTOR}->" +
                    "setMainActivity(Landroid/app/Activity;)V",
        )

        // Find the obfuscated method name for PlaybackStartDescriptor.videoId()
        val playbackStartVideoIdMethodName = playbackStartFeatureFlagFingerprint.method.let {
            val stringMethodIndex = it.indexOfFirstInstructionOrThrow {
                val reference = getReference<MethodReference>()
                reference?.definingClass == "Lcom/google/android/libraries/youtube/player/model/PlaybackStartDescriptor;"
                        && reference.returnType == "Ljava/lang/String;"
            }

            navigate(it).to(stringMethodIndex).stop().name
        }

        fun extensionInstructions(playbackStartRegister: Int, freeRegister: Int) =
            """
                invoke-virtual { v$playbackStartRegister }, Lcom/google/android/libraries/youtube/player/model/PlaybackStartDescriptor;->$playbackStartVideoIdMethodName()Ljava/lang/String;
                move-result-object v$freeRegister
                invoke-static { v$freeRegister }, $EXTENSION_CLASS_DESCRIPTOR->openShort(Ljava/lang/String;)Z
                move-result v$freeRegister
                if-eqz v$freeRegister, :disabled
                return-void
                
                :disabled
                nop
            """

        if (!is_19_25_or_greater) {
            shortsPlaybackIntentLegacyFingerprint.method.apply {
                val index = indexOfFirstInstructionOrThrow {
                    getReference<MethodReference>()?.returnType ==
                            "Lcom/google/android/libraries/youtube/player/model/PlaybackStartDescriptor;"
                }
                val freeRegister = getInstruction<FiveRegisterInstruction>(index).registerC
                val playbackStartRegister = getInstruction<OneRegisterInstruction>(index + 1).registerA

                addInstructionsWithLabels(
                    index + 2,
                    extensionInstructions(playbackStartRegister, freeRegister)
                )
            }

            return@execute
        }

        shortsPlaybackIntentFingerprint.method.addInstructionsWithLabels(
            0,
            """
                move-object/from16 v0, p1
                ${extensionInstructions(0, 1)}
            """
        )
    }
}
