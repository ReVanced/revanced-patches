package app.revanced.extension.youtube.settings.preference;

import android.content.Context;
import android.util.AttributeSet;
import app.revanced.extension.shared.settings.preference.UrlLinkPreference;

/**
 * Allows tapping the DeArrow about preference to open the DeArrow website.
 */
@SuppressWarnings("unused")
public class AlternativeThumbnailsAboutDeArrowPreference extends UrlLinkPreference {
    {
        externalUrl = "https://dearrow.ajay.app";
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
