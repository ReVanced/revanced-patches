package app.revanced.extension.youtube.returnyoutubedislike.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.util.AttributeSet;

/**
 * Allows tapping the RYD about preference to open the website.
 */
@SuppressWarnings({"unused", "deprecation"})
public class ReturnYouTubeDislikeAboutPreference extends Preference {
    {
        setOnPreferenceClickListener(pref -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse("https://returnyoutubedislike.com"));
            pref.getContext().startActivity(i);
            return false;
        });
    }

    public ReturnYouTubeDislikeAboutPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    public ReturnYouTubeDislikeAboutPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public ReturnYouTubeDislikeAboutPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ReturnYouTubeDislikeAboutPreference(Context context) {
        super(context);
    }
}
