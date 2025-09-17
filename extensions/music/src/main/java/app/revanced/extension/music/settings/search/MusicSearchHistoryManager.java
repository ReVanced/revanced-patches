package app.revanced.extension.music.settings.search;

import android.app.Activity;
import android.widget.FrameLayout;

import app.revanced.extension.music.settings.Settings;
import app.revanced.extension.shared.settings.BooleanSetting;
import app.revanced.extension.shared.settings.StringSetting;
import app.revanced.extension.shared.settings.search.BaseSearchHistoryManager;

/**
 * Music-specific search history manager.
 */
public class MusicSearchHistoryManager extends BaseSearchHistoryManager {

    public MusicSearchHistoryManager(Activity activity, FrameLayout overlayContainer,
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
