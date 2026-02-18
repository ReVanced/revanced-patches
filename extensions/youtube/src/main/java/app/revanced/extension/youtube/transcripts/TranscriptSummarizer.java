package app.revanced.extension.youtube.transcripts;

import static app.revanced.extension.shared.StringRef.str;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.patches.VideoInformation;
import app.revanced.extension.youtube.settings.Settings;

/**
 * Manages the video transcript summarization flow.
 * Coordinates fetching, API calls, caching, and result display.
 */
public class TranscriptSummarizer {
    private static final String TAG = "TranscriptSummarizer";
    
    /**
     * Stores the player response for use in transcript fetching.
     */
    @Nullable
    private static volatile String currentPlayerResponse = null;
    
    /**
     * Current video ID being processed.
     */
    @Nullable
    private static volatile String processingVideoId = null;
    
    /**
     * Flag to prevent multiple concurrent summarizations.
     */
    private static volatile boolean isProcessing = false;

    /**
     * Called by patch to store player response data.
     * Hook signature matches PlayerResponseMethodHookPatch ProtoBufferParameter.
     * 
     * @param playerResponse The player response JSON string
     * @param videoId The video ID
     * @param isShort Whether this is a Short video
     */
    public static void setPlayerResponse(@NonNull String playerResponse, 
                                         @NonNull String videoId, 
                                         boolean isShort) {
        currentPlayerResponse = playerResponse;
        Logger.printDebug(() -> TAG + " Player response updated for video: " + videoId + 
            " (isShort: " + isShort + ")");
    }

    /**
     * Summarize the currently playing video.
     * Called when the user clicks the summarize button.
     */
    public static void summarizeCurrentVideo(@NonNull Context context) {
        if (!Settings.SUMMARIZE_TRANSCRIPT_ENABLED.get()) {
            Logger.printDebug(() -> TAG + " Feature is disabled");
            return;
        }

        String apiKey = Settings.GEMINI_API_KEY.get();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            Toast.makeText(context, 
                str("revanced_summarize_no_api_key"), 
                Toast.LENGTH_LONG).show();
            Logger.printDebug(() -> TAG + " No API key configured");
            return;
        }

        String videoId = VideoInformation.getVideoId();
        if (videoId == null || videoId.isEmpty()) {
            Toast.makeText(context, 
                str("revanced_summarize_no_video"), 
                Toast.LENGTH_SHORT).show();
            Logger.printDebug(() -> TAG + " No video ID available");
            return;
        }

        // Check if already processing this video
        if (isProcessing && videoId.equals(processingVideoId)) {
            Toast.makeText(context, 
                str("revanced_summarize_already_processing"), 
                Toast.LENGTH_SHORT).show();
            Logger.printDebug(() -> TAG + " Already processing this video");
            return;
        }

        // Check cache first
        TranscriptSummaryCache.CachedSummary cached = TranscriptSummaryCache.get(videoId);
        if (cached != null) {
            Logger.printDebug(() -> TAG + " Using cached summary for: " + videoId);
            displaySummary(context, cached.summary, true);
            return;
        }

        // Start async summarization
        startSummarization(context, videoId, apiKey);
    }

    /**
     * Start the async summarization process.
     */
    private static void startSummarization(@NonNull Context context, 
                                          @NonNull String videoId, 
                                          @NonNull String apiKey) {
        isProcessing = true;
        processingVideoId = videoId;

        Toast.makeText(context, 
            str("revanced_summarize_fetching_transcript"), 
            Toast.LENGTH_SHORT).show();

        Utils.runOnBackgroundThread(() -> {
            try {
                processSummarization(context, videoId, apiKey);
            } finally {
                isProcessing = false;
                processingVideoId = null;
            }
        });
    }

    /**
     * Process the summarization (runs on background thread).
     */
    private static void processSummarization(@NonNull Context context, 
                                            @NonNull String videoId,
                                            @NonNull String apiKey) {
        Logger.printDebug(() -> TAG + " Starting summarization for video: " + videoId);

        // Step 1: Fetch transcript
        if (currentPlayerResponse == null) {
            showError(context, str("revanced_summarize_no_player_response"));
            Logger.printDebug(() -> TAG + " No player response available");
            return;
        }

        // Get user's preferred language (could be enhanced to detect from system)
        String preferredLanguage = "en"; // Default to English
        
        TranscriptFetcher.Transcript transcript = TranscriptFetcher.fetchTranscriptForVideo(
            videoId, currentPlayerResponse, preferredLanguage
        );

        if (transcript == null) {
            showError(context, str("revanced_summarize_no_transcript"));
            Logger.printDebug(() -> TAG + " Failed to fetch transcript");
            return;
        }

        Logger.printDebug(() -> TAG + " Transcript fetched: " + transcript.text.length() + " chars");

        // Show progress update
        Utils.runOnMainThread(() -> 
            Toast.makeText(context, 
                str("revanced_summarize_generating"), 
                Toast.LENGTH_SHORT).show()
        );

        // Step 2: Summarize with Gemini API
        GeminiApi.SummaryResult result = GeminiApi.summarize(apiKey, transcript.text);

        if (!result.success || result.summary == null) {
            showError(context, str("revanced_summarize_api_error", result.errorMessage));
            Logger.printDebug(() -> TAG + " Summarization failed: " + result.errorMessage);
            return;
        }

        Logger.printDebug(() -> TAG + " Summary generated successfully");

        // Step 3: Cache the result
        TranscriptSummaryCache.put(videoId, result.summary, "gemini-1.5-flash");

        // Step 4: Display summary
        displaySummary(context, result.summary, false);
    }

    /**
     * Display the summary to the user (runs on main thread).
     */
    private static void displaySummary(@NonNull Context context, 
                                      @NonNull String summary, 
                                      boolean fromCache) {
        Utils.runOnMainThread(() -> {
            // Show success toast
            String message = fromCache ? 
                str("revanced_summarize_success_cached") : 
                str("revanced_summarize_success");
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

            // For now, display in a dialog
            // In phase 6, we'll inject into description section instead
            SummaryViewer.showSummaryDialog(context, summary);
            
            Logger.printDebug(() -> TAG + " Summary displayed to user");
        });
    }

    /**
     * Show error message on main thread.
     */
    private static void showError(@NonNull Context context, @NonNull String message) {
        Utils.runOnMainThread(() -> 
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        );
    }

    /**
     * Reset state when video changes.
     */
    public static void onVideoChanged() {
        Logger.printDebug(() -> TAG + " Video changed, resetting state");
        // Don't clear player response - it will be updated by patch
    }

    /**
     * Initialize the summarizer system.
     */
    public static void initialize() {
        TranscriptSummaryCache.initialize();
        Logger.printDebug(() -> TAG + " Summarizer initialized");
    }
}
