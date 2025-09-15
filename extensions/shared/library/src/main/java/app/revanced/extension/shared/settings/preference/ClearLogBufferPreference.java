package app.revanced.extension.shared.settings.preference;

import android.content.Context;
import android.util.AttributeSet;
import android.preference.Preference;

/**
 * A custom preference that clears the ReVanced debug log buffer when clicked.
 * Invokes the {@link LogBufferManager#clearLogBuffer} method.
 */
@SuppressWarnings("unused")
public class ClearLogBufferPreference extends Preference {

    {
        setOnPreferenceClickListener(pref -> {
            LogBufferManager.clearLogBuffer();
            return true;
        });
    }

    public ClearLogBufferPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    public ClearLogBufferPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public ClearLogBufferPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ClearLogBufferPreference(Context context) {
        super(context);
    }
}
