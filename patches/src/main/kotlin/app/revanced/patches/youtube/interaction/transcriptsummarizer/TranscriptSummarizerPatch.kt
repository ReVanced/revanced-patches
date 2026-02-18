package app.revanced.patches.youtube.interaction.transcriptsummarizer

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference.Sorting
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.shared.misc.settings.preference.TextPreference
import app.revanced.patches.youtube.misc.playercontrols.addBottomControl
import app.revanced.patches.youtube.misc.playercontrols.initializeBottomControl
import app.revanced.patches.youtube.misc.playercontrols.injectVisibilityCheckCall
import app.revanced.patches.youtube.misc.playercontrols.playerControlsPatch
import app.revanced.patches.youtube.misc.playercontrols.playerControlsResourcePatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.shared.mainActivityOnCreateFingerprint
import app.revanced.patches.youtube.video.information.videoInformationPatch
import app.revanced.patches.youtube.video.playerresponse.Hook
import app.revanced.patches.youtube.video.playerresponse.addPlayerResponseMethodHook
import app.revanced.patches.youtube.video.playerresponse.playerResponseMethodHookPatch
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources

private val transcriptSummarizerResourcePatch = resourcePatch {
    dependsOn(
        playerControlsResourcePatch,
        settingsPatch,
        addResourcesPatch,
    )

    execute {
        addResources("youtube", "interaction.transcriptsummarizer.transcriptSummarizerResourcePatch")

        PreferenceScreen.PLAYER.addPreferences(
            PreferenceScreenPreference(
                key = "revanced_transcript_summarizer_screen",
                sorting = Sorting.UNSORTED,
                preferences = setOf(
                    SwitchPreference("revanced_summarize_transcript_enabled"),
                    TextPreference(
                        "revanced_gemini_api_key",
                        tag = "app.revanced.extension.youtube.settings.preference.GeminiApiKeyPreference",
                    ),
                    SwitchPreference("revanced_summarize_show_button"),
                    SwitchPreference("revanced_summarize_auto_expand"),
                    TextPreference(
                        "revanced_clear_transcript_cache",
                        tag = "app.revanced.extension.youtube.settings.preference.ClearTranscriptCachePreference",
                    ),
                ),
            ),
        )

        copyResources(
            "transcriptsummarizer",
            ResourceGroup("drawable", "revanced_summarize_button.xml"),
        )

        addBottomControl("transcriptsummarizer")
    }
}

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/transcripts/TranscriptSummarizer;"

internal const val BUTTON_DESCRIPTOR = "Lapp/revanced/extension/youtube/videoplayer/SummarizeButton;"

@Suppress("unused")
val transcriptSummarizerPatch = bytecodePatch(
    name = "Transcript summarizer",
    description = "Adds support to summarize video transcripts using AI (Google Gemini). " +
        "Provides a player button to generate and view summaries.",
) {
    dependsOn(
        transcriptSummarizerResourcePatch,
        playerControlsPatch,
        videoInformationPatch,
        playerResponseMethodHookPatch,
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
        initializeBottomControl(BUTTON_DESCRIPTOR)
        injectVisibilityCheckCall(BUTTON_DESCRIPTOR)

        // Initialize the summarizer when the main activity is created
        mainActivityOnCreateFingerprint.method.addInstruction(
            0,
            "invoke-static {}, $EXTENSION_CLASS_DESCRIPTOR->initialize()V"
        )

        // Hook player response to extract caption data
        addPlayerResponseMethodHook(
            Hook.ProtoBufferParameter(
                "$EXTENSION_CLASS_DESCRIPTOR->setPlayerResponse(Ljava/lang/String;Ljava/lang/String;Z)V"
            )
        )
    }
}
