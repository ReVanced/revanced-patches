package app.revanced.extension.youtube.settings.preference;

import android.content.Context;
import android.util.AttributeSet;
import android.preference.Preference;
import app.revanced.extension.shared.settings.preference.LogBufferManager;

/**
 * A custom preference that triggers exporting ReVanced debug logs to the clipboard when clicked.
 * Invokes the exportLogcatToClipboard method from LogBufferManager.
 */
@SuppressWarnings("unused")
public class ExportLogcatPreference extends Preference {

    {
        setOnPreferenceClickListener(pref -> {
            LogBufferManager.exportToClipboard(getContext());
            return true;
        });
    }

    public ExportLogcatPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    public ExportLogcatPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public ExportLogcatPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ExportLogcatPreference(Context context) {
        super(context);
    }
}
