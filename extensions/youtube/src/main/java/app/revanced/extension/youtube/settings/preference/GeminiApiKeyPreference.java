package app.revanced.extension.youtube.settings.preference;

import static app.revanced.extension.shared.StringRef.str;

import android.app.AlertDialog;
import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.widget.Toast;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.youtube.transcripts.GeminiApi;

/**
 * Custom preference for Gemini API key with validation.
 */
@SuppressWarnings("unused")
public class GeminiApiKeyPreference extends EditTextPreference {
    private static final String TAG = "GeminiApiKeyPreference";

    public GeminiApiKeyPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public GeminiApiKeyPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public GeminiApiKeyPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GeminiApiKeyPreference(Context context) {
        super(context);
        init();
    }

    private void init() {
        setTitle(str("revanced_gemini_api_key_title"));
        setSummary(str("revanced_gemini_api_key_summary"));
        setDialogTitle(str("revanced_gemini_api_key_dialog_title"));
        
        // Set input type to password mode for security
        getEditText().setInputType(android.text.InputType.TYPE_CLASS_TEXT 
            | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            String apiKey = getEditText().getText().toString().trim();
            
            // Validate API key format
            if (!apiKey.isEmpty() && !GeminiApi.isValidApiKeyFormat(apiKey)) {
                // Show error toast
                Toast.makeText(getContext(), 
                    str("revanced_gemini_api_key_invalid_format"), 
                    Toast.LENGTH_LONG).show();
                
                Logger.printDebug(() -> TAG + " Invalid API key format provided");
                return; // Don't save invalid key
            }
            
            if (!apiKey.isEmpty()) {
                // Show informational toast about key being saved
                Toast.makeText(getContext(), 
                    str("revanced_gemini_api_key_saved"), 
                    Toast.LENGTH_SHORT).show();
                
                Logger.printDebug(() -> TAG + " API key saved successfully");
            }
        }
        
        super.onDialogClosed(positiveResult);
    }
}
