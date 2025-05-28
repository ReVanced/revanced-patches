package app.revanced.extension.youtube.settings.preference;

import static android.text.Html.FROM_HTML_MODE_COMPACT;

import android.content.Context;
import android.preference.Preference;
import android.text.Html;
import android.util.AttributeSet;

/**
 * Allows using basic html for the summary text.
 */
@SuppressWarnings({"unused", "deprecation"})
public class HtmlPreference extends Preference {
    {
        setSummary(Html.fromHtml(getSummary().toString(), FROM_HTML_MODE_COMPACT));
    }

    public HtmlPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public HtmlPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public HtmlPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HtmlPreference(Context context) {
        super(context);
    }
}
