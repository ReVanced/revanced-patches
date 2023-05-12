package app.revanced.integrations.patches;

import android.view.View;
import app.revanced.integrations.adremover.AdRemoverAPI;
import app.revanced.integrations.settings.SettingsEnum;

public final class HideFilterBarPatch {
    public static int hideInFeed(final int height) {
        if (SettingsEnum.HIDE_FILTER_BAR_FEED_IN_FEED.getBoolean()) return 0;

        return height;
    }

    public static void hideInRelatedVideos(final View chipView) {
        if (!SettingsEnum.HIDE_FILTER_BAR_FEED_IN_RELATED_VIDEOS.getBoolean()) return;

        AdRemoverAPI.HideViewWithLayout1dp(chipView);
    }

    public static int hideInSearch(final int height) {
        if (SettingsEnum.HIDE_FILTER_BAR_FEED_IN_SEARCH.getBoolean()) return 0;

        return height;
    }
}
