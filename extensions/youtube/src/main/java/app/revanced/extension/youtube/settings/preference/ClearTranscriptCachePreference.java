package app.revanced.extension.youtube.settings.preference;

import static app.revanced.extension.shared.StringRef.str;

import android.app.AlertDialog;
import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.widget.Toast;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.youtube.transcripts.TranscriptSummaryCache;

/**
 * Preference that clears the transcript summary cache when clicked.
 */
@SuppressWarnings("unused")
public class ClearTranscriptCachePreference extends Preference {
    private static final String TAG = "ClearTranscriptCachePreference";

    public ClearTranscriptCachePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public ClearTranscriptCachePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ClearTranscriptCachePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ClearTranscriptCachePreference(Context context) {
        super(context);
        init();
    }

    private void init() {
        setTitle(str("revanced_clear_transcript_cache_title"));
        updateSummary();
        
        setOnPreferenceClickListener(preference -> {
            showConfirmDialog();
            return true;
        });
    }

    private void updateSummary() {
        int cacheSize = TranscriptSummaryCache.size();
        String formattedSize = TranscriptSummaryCache.formatCacheSize();
        
        setSummary(str("revanced_clear_transcript_cache_summary", cacheSize, formattedSize));
    }

    private void showConfirmDialog() {
        int cacheSize = TranscriptSummaryCache.size();
        
        if (cacheSize == 0) {
            Toast.makeText(getContext(), 
                str("revanced_clear_transcript_cache_empty"), 
                Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(getContext())
            .setTitle(str("revanced_clear_transcript_cache_dialog_title"))
            .setMessage(str("revanced_clear_transcript_cache_dialog_message", cacheSize))
            .setPositiveButton(str("revanced_clear_transcript_cache_clear"), (dialog, which) -> {
                clearCache();
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
    }

    private void clearCache() {
        try {
            TranscriptSummaryCache.clearAll();
            updateSummary();
            
            Toast.makeText(getContext(), 
                str("revanced_clear_transcript_cache_success"), 
                Toast.LENGTH_SHORT).show();
            
            Logger.printDebug(() -> TAG + " Cache cleared successfully");
        } catch (Exception e) {
            Toast.makeText(getContext(), 
                str("revanced_clear_transcript_cache_error"), 
                Toast.LENGTH_LONG).show();
            
            Logger.printException(() -> TAG + " Failed to clear cache", e);
        }
    }

    @Override
    protected void onAttachedToHierarchy(android.preference.PreferenceManager preferenceManager) {
        super.onAttachedToHierarchy(preferenceManager);
        // Update summary when preference screen is shown
        updateSummary();
    }
}
