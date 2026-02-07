package app.revanced.extension.shared.settings.preference;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.util.AttributeSet;

import app.revanced.extension.shared.Logger;

/**
 * Simple preference that opens a URL when clicked.
 */
@SuppressWarnings("deprecation")
public class URLLinkPreference extends Preference {

    protected String externalURL;

    {
        setOnPreferenceClickListener(pref -> {
            if (externalURL == null) {
                Logger.printException(() -> "URL not set " + getClass().getSimpleName());
                return false;
            }
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(externalURL));
            pref.getContext().startActivity(i);
            return true;
        });
    }

    public URLLinkPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    public URLLinkPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public URLLinkPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public URLLinkPreference(Context context) {
        super(context);
    }
}
