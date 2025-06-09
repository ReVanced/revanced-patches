package app.revanced.extension.youtube.settings.preference;

import android.content.Context;
import android.util.AttributeSet;

import app.revanced.extension.shared.settings.preference.SortedListPreference;

/**
 * A custom ListPreference that uses a styled custom dialog with a custom checkmark indicator.
 */
@SuppressWarnings("unused")
public class CustomDialogListPreference extends SortedListPreference {

    public CustomDialogListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomDialogListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomDialogListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomDialogListPreference(Context context) {
        super(context);
    }

    @Override
    protected int getFirstEntriesToPreserve() {
        return -1; // Do not sort.
    }
}
