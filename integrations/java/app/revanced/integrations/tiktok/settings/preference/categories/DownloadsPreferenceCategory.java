package app.revanced.integrations.tiktok.settings.preference.categories;

import android.content.Context;
import android.preference.PreferenceScreen;
import app.revanced.integrations.tiktok.settings.Settings;
import app.revanced.integrations.tiktok.settings.SettingsStatus;
import app.revanced.integrations.tiktok.settings.preference.DownloadPathPreference;
import app.revanced.integrations.tiktok.settings.preference.TogglePreference;

@SuppressWarnings("deprecation")
public class DownloadsPreferenceCategory extends ConditionalPreferenceCategory {
    public DownloadsPreferenceCategory(Context context, PreferenceScreen screen) {
        super(context, screen);
        setTitle("Downloads");
    }

    @Override
    public boolean getSettingsStatus() {
        return SettingsStatus.downloadEnabled;
    }

    @Override
    public void addPreferences(Context context) {
        addPreference(new DownloadPathPreference(
                context,
                "Download path",
                Settings.DOWNLOAD_PATH
        ));
        addPreference(new TogglePreference(
                context,
                "Remove watermark", "",
                Settings.DOWNLOAD_WATERMARK
        ));
    }
}
