package app.revanced.extension.youtube.sponsorblock.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.util.AttributeSet;

@SuppressWarnings({"unused", "deprecation"})
public class SponsorBlockAboutPreference extends Preference {
    {
        setOnPreferenceClickListener(pref -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse("https://sponsor.ajay.app"));
            pref.getContext().startActivity(i);
            return false;
        });
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
