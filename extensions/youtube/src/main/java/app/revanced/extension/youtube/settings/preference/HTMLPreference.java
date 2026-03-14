package app.revanced.extension.youtube.settings.preference;

import static android.text.Html.FROM_HTML_MODE_COMPACT;

import android.content.Context;
import android.preference.Preference;
import android.text.Html;
import android.util.AttributeSet;

/**
 * Allows using basic HTML for the summary text.
 */
@SuppressWarnings({"unused", "deprecation"})
public class HTMLPreference extends Preference {
    {
        setSummary(Html.fromHtml(getSummary().toString(), FROM_HTML_MODE_COMPACT));
    }

    public HTMLPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public HTMLPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public HTMLPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HTMLPreference(Context context) {
        super(context);
    }
}
