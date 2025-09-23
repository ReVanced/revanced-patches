package app.revanced.patches.youtube.layout.shortsplayer

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
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
import app.revanced.util.findFreeRegister
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/OpenShortsInRegularPlayerPatch;"

internal var mdx_drawer_layout_id = -1L
    private set

private val openShortsInRegularPlayerResourcePatch = resourcePatch {
    dependsOn(resourceMappingPatch)

    execute {
        mdx_drawer_layout_id = resourceMappings[
            "id",
            "mdx_drawer_layout",
        ]

    }
}

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
        versionCheckPatch,
        openShortsInRegularPlayerResourcePatch
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
        addResources("youtube", "layout.shortsplayer.shortsPlayerTypePatch")

        PreferenceScreen.SHORTS.addPreferences(
            if (is_19_46_or_greater) {
                ListPreference("revanced_shorts_player_type")
            } else {
                ListPreference(
                    key = "revanced_shorts_player_type",
                    entriesKey = "revanced_shorts_player_type_legacy_entries",
                    entryValuesKey = "revanced_shorts_player_type_legacy_entry_values"
                )
            }
        )

        // Activity is used as the context to launch an Intent.
        mainActivityOnCreateFingerprint.method.addInstruction(
            0,
            "invoke-static/range { p0 .. p0 }, $EXTENSION_CLASS_DESCRIPTOR->" +
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
                val playbackStartRegister = getInstruction<OneRegisterInstruction>(index + 1).registerA
                val insertIndex = index + 2
                val freeRegister = findFreeRegister(insertIndex, playbackStartRegister)

                addInstructionsWithLabels(
                    insertIndex,
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

        // Fix issue with back button exiting the app instead of minimizing the player.
        // Without this change this issue can be difficult to reproduce, but seems to occur
        // most often with 'open video in regular player' and not open in fullscreen player.
        exitVideoPlayerFingerprint.method.apply {
            // Method call for Activity.finish()
            val finishIndex = indexOfFirstInstructionOrThrow {
                val reference = getReference<MethodReference>()
                reference?.name == "finish"
            }

            // Index of PlayerType.isWatchWhileMaximizedOrFullscreen()
            val index = indexOfFirstInstructionReversedOrThrow(finishIndex, Opcode.MOVE_RESULT)
            val register = getInstruction<OneRegisterInstruction>(index).registerA

            addInstructions(
                index + 1,
                """
                    invoke-static { v$register }, ${EXTENSION_CLASS_DESCRIPTOR}->overrideBackPressToExit(Z)Z    
                    move-result v$register
                """
            )
        }
    }
}
