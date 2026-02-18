package app.revanced.extension.youtube.videoplayer;

import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.youtube.patches.VideoInformation;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.transcripts.TranscriptSummarizer;

/**
 * Player button for video transcript summarization.
 */
@SuppressWarnings("unused")
public class SummarizeButton {
    private static final String TAG = "SummarizeButton";
    
    @Nullable
    private static PlayerControlButton instance;

    /**
     * Injection point.
     */
    public static void initializeButton(View controlsView) {
        try {
            instance = new PlayerControlButton(
                    controlsView,
                    "revanced_summarize_button",
                    null,
                    () -> Settings.SUMMARIZE_TRANSCRIPT_ENABLED.get() && 
                          Settings.SUMMARIZE_SHOW_BUTTON.get(),
                    SummarizeButton::onSummarizeClick,
                    null
            );
            
            Logger.printDebug(() -> TAG + " Button initialized");
        } catch (Exception ex) {
            Logger.printException(() -> TAG + " initializeButton failure", ex);
        }
    }

    /**
     * Injection point.
     */
    public static void setVisibilityNegatedImmediate() {
        if (instance != null) instance.setVisibilityNegatedImmediate();
    }

    /**
     * Injection point.
     */
    public static void setVisibilityImmediate(boolean visible) {
        if (instance != null) instance.setVisibilityImmediate(visible);
    }

    /**
     * Injection point.
     */
    public static void setVisibility(boolean visible, boolean animated) {
        if (instance != null) instance.setVisibility(visible, animated);
    }

    /**
     * Handle button click - trigger summarization.
     */
    private static void onSummarizeClick(View view) {
        try {
            String videoId = VideoInformation.getVideoId();
            if (videoId == null || videoId.isEmpty()) {
                Logger.printDebug(() -> TAG + " No video ID available");
                return;
            }

            Logger.printDebug(() -> TAG + " Summarize button clicked for video: " + videoId);
            
            // Delegate to the summarizer manager
            TranscriptSummarizer.summarizeCurrentVideo(view.getContext());
            
        } catch (Exception ex) {
            Logger.printException(() -> TAG + " onSummarizeClick failure", ex);
            Toast.makeText(view.getContext(), 
                "Failed to start summarization", 
                Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Update button state (e.g., show loading indicator).
     */
    public static void setLoading(boolean loading) {
        if (instance != null) {
            // Could change icon or disable button during loading
            // This would require additional button state management
            Logger.printDebug(() -> TAG + " Loading state: " + loading);
        }
    }
}
