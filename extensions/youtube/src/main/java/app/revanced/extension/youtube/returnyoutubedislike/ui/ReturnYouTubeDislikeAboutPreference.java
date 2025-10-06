package app.revanced.extension.youtube.returnyoutubedislike.ui;

import android.content.Context;
import android.util.AttributeSet;

import app.revanced.extension.shared.settings.preference.UrlLinkPreference;

/**
 * Allows tapping the RYD about preference to open the website.
 */
@SuppressWarnings("unused")
public class ReturnYouTubeDislikeAboutPreference extends UrlLinkPreference {
    {
        externalUrl = "https://returnyoutubedislike.com";
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
