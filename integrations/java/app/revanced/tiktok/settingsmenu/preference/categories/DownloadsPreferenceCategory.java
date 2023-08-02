package app.revanced.tiktok.settingsmenu.preference.categories;

import android.content.Context;
import android.preference.PreferenceScreen;
import app.revanced.tiktok.settings.SettingsEnum;
import app.revanced.tiktok.settingsmenu.SettingsStatus;
import app.revanced.tiktok.settingsmenu.preference.DownloadPathPreference;
import app.revanced.tiktok.settingsmenu.preference.TogglePreference;

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
                SettingsEnum.DOWNLOAD_PATH
        ));
        addPreference(new TogglePreference(
                context,
                "Remove watermark", "",
                SettingsEnum.DOWNLOAD_WATERMARK
        ));
    }
}
