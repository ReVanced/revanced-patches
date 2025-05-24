package app.revanced.extension.youtube.settings.preference;

import android.content.Context;
import android.util.AttributeSet;
import android.preference.Preference;
import app.revanced.extension.shared.settings.preference.LogBufferManager;

/**
 * A custom preference that triggers exporting ReVanced debug logs to the clipboard when clicked.
 * Invokes the exportLogcatToClipboard method from LogBufferManager.
 */
@SuppressWarnings({"deprecation", "unused"})
public class ExportLogToClipboardPreference extends Preference {

    {
        setOnPreferenceClickListener(pref -> {
            LogBufferManager.exportToClipboard();
            return true;
        });
    }

    public ExportLogToClipboardPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    public ExportLogToClipboardPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public ExportLogToClipboardPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ExportLogToClipboardPreference(Context context) {
        super(context);
    }
}
