package app.revanced.integrations.tiktok.settings.preference.categories;

import android.content.Context;
import android.preference.PreferenceScreen;
import app.revanced.integrations.tiktok.settings.preference.RangeValuePreference;
import app.revanced.integrations.tiktok.settings.Settings;
import app.revanced.integrations.tiktok.settings.SettingsStatus;
import app.revanced.integrations.tiktok.settings.preference.TogglePreference;

@SuppressWarnings("deprecation")
public class FeedFilterPreferenceCategory extends ConditionalPreferenceCategory {
    public FeedFilterPreferenceCategory(Context context, PreferenceScreen screen) {
        super(context, screen);
        setTitle("Feed filter");
    }

    @Override
    public boolean getSettingsStatus() {
        return SettingsStatus.feedFilterEnabled;
    }

    @Override
    public void addPreferences(Context context) {
        addPreference(new TogglePreference(
                context,
                "Remove feed ads", "Remove ads from feed.",
                Settings.REMOVE_ADS
        ));
        addPreference(new TogglePreference(
                context,
                "Hide livestreams", "Hide livestreams from feed.",
                Settings.HIDE_LIVE
        ));
        addPreference(new TogglePreference(
                context,
                "Hide story", "Hide story from feed.",
                Settings.HIDE_STORY
        ));
        addPreference(new TogglePreference(
                context,
                "Hide image video", "Hide image video from feed.",
                Settings.HIDE_IMAGE
        ));
        addPreference(new RangeValuePreference(
                context,
                "Min/Max views", "The minimum or maximum views of a video to show.",
                Settings.MIN_MAX_VIEWS
        ));
        addPreference(new RangeValuePreference(
                context,
                "Min/Max likes", "The minimum or maximum likes of a video to show.",
                Settings.MIN_MAX_LIKES
        ));
    }
}
