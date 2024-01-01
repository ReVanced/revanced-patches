package app.revanced.integrations.youtube.settings.preference;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.util.AttributeSet;

/**
 * Allows tapping the DeArrow about preference to open the DeArrow website.
 */
@SuppressWarnings("unused")
public class AlternativeThumbnailsAboutDeArrowPreference extends Preference {
    {
        setOnPreferenceClickListener(pref -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse("https://dearrow.ajay.app"));
            pref.getContext().startActivity(i);
            return false;
        });
    }

    public AlternativeThumbnailsAboutDeArrowPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    public AlternativeThumbnailsAboutDeArrowPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public AlternativeThumbnailsAboutDeArrowPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public AlternativeThumbnailsAboutDeArrowPreference(Context context) {
        super(context);
    }
}
