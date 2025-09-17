package app.revanced.extension.youtube.settings.search;

import android.app.Activity;
import android.widget.FrameLayout;

import app.revanced.extension.shared.settings.BooleanSetting;
import app.revanced.extension.shared.settings.StringSetting;
import app.revanced.extension.shared.settings.search.BaseSearchHistoryManager;
import app.revanced.extension.youtube.settings.Settings;

/**
 * YouTube-specific search history manager.
 */
public class YouTubeSearchHistoryManager extends BaseSearchHistoryManager {

    public YouTubeSearchHistoryManager(Activity activity, FrameLayout overlayContainer,
                                       OnSelectHistoryItemListener onSelectHistoryItemAction) {
        super(activity, overlayContainer, onSelectHistoryItemAction);
    }

    @Override
    protected BooleanSetting getSearchHistorySetting() {
        return Settings.SETTINGS_SEARCH_HISTORY;
    }

    @Override
    protected StringSetting getSearchEntriesSetting() {
        return Settings.SETTINGS_SEARCH_ENTRIES;
    }
}
