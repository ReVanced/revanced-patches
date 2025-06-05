package app.revanced.extension.youtube.settings.preference;

import android.content.Context;
import android.util.AttributeSet;

import app.revanced.extension.shared.settings.preference.ReVancedAboutPreference;
import app.revanced.extension.youtube.ThemeHelper;

@SuppressWarnings("unused")
public class ReVancedYouTubeAboutPreference extends ReVancedAboutPreference {

    public int getLightColor() {
        return ThemeHelper.getLightThemeColor();
    }

    public int getDarkColor() {
        return ThemeHelper.getDarkThemeColor();
    }

    public ReVancedYouTubeAboutPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    public ReVancedYouTubeAboutPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public ReVancedYouTubeAboutPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ReVancedYouTubeAboutPreference(Context context) {
        super(context);
    }
}
