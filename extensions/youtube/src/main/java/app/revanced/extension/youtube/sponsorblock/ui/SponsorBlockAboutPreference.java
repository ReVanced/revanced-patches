package app.revanced.extension.youtube.sponsorblock.ui;

import android.content.Context;
import android.util.AttributeSet;

import app.revanced.extension.shared.settings.preference.UrlLinkPreference;

@SuppressWarnings("unused")
public class SponsorBlockAboutPreference extends UrlLinkPreference {
    {
        externalUrl = "https://sponsor.ajay.app";
    }

    public SponsorBlockAboutPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    public SponsorBlockAboutPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public SponsorBlockAboutPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public SponsorBlockAboutPreference(Context context) {
        super(context);
    }
}
