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
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.shared.mainActivityOnCreateFingerprint
import app.revanced.util.findFreeRegister
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

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
            ListPreference(
                key = "revanced_shorts_player_type",
                summaryKey = null
            )
        )

        // Activity is used as the context to launch an Intent.
        mainActivityOnCreateFingerprint.method.addInstruction(
            1,
            "invoke-static/range { p0 .. p0 }, ${EXTENSION_CLASS_DESCRIPTOR}->" +
                    "setMainActivity(Landroid/app/Activity;)V",
        )

        // Find the obfuscated method name for PlaybackStartDescriptor.videoId()
        val playbackStartVideoIdMethodName = playbackStartFeatureFlagFingerprint.let {
            val stringMethodIndex = it.instructionMatches.first().index
            it.method.let {
                navigate(it).to(stringMethodIndex).stop().name
            }
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

        if (is_19_25_or_greater) {
            shortsPlaybackIntentFingerprint.method.addInstructionsWithLabels(
                0,
                """
                    move-object/from16 v0, p1
                    ${extensionInstructions(0, 1)}
                """
            )
        } else {
            shortsPlaybackIntentLegacyFingerprint.let {
                it.method.apply {
                    val index = it.instructionMatches.first().index
                    val playbackStartRegister = getInstruction<OneRegisterInstruction>(index + 1).registerA
                    val insertIndex = index + 2
                    val freeRegister = findFreeRegister(insertIndex, playbackStartRegister)

                    addInstructionsWithLabels(
                        insertIndex,
                        extensionInstructions(playbackStartRegister, freeRegister)
                    )
                }
            }
        }
    }
}
