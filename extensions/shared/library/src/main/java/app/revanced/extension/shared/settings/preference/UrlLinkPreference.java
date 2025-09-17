package app.revanced.extension.shared.settings.preference;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.util.AttributeSet;

import app.revanced.extension.shared.Logger;

/**
 * Simple preference that opens a url when clicked.
 */
@SuppressWarnings("deprecation")
public class UrlLinkPreference extends Preference {

    protected String externalUrl;

    {
        setOnPreferenceClickListener(pref -> {
            if (externalUrl == null) {
                Logger.printException(() -> "URL not set " + getClass().getSimpleName());
                return false;
            }
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(externalUrl));
            pref.getContext().startActivity(i);
            return true;
        });
    }

    public UrlLinkPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    public UrlLinkPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public UrlLinkPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public UrlLinkPreference(Context context) {
        super(context);
    }
}
