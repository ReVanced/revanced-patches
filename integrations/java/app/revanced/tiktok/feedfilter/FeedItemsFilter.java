package app.revanced.tiktok.feedfilter;

import com.ss.android.ugc.aweme.feed.model.Aweme;
import com.ss.android.ugc.aweme.feed.model.FeedItemList;

import java.util.Iterator;
import java.util.List;

import app.revanced.tiktok.settings.SettingsEnum;

public class FeedItemsFilter {

    public static void filter(FeedItemList feedItemList) {
        if (SettingsEnum.TIK_REMOVE_ADS.getBoolean()) removeAds(feedItemList);
        if (SettingsEnum.TIK_HIDE_LIVE.getBoolean()) removeLive(feedItemList);
    }

    private static void removeAds(FeedItemList feedItemList) {
        List<Aweme> items = feedItemList.items;
        Iterator<Aweme> it = items.iterator();
        while (it.hasNext()) {
            Aweme item = it.next();
            if (item != null && (item.isAd() || item.isWithPromotionalMusic())) {
                it.remove();
            }
        }
    }

    private static void removeLive(FeedItemList feedItemList) {
        List<Aweme> items = feedItemList.items;
        Iterator<Aweme> it = items.iterator();
        while (it.hasNext()) {
            Aweme item = it.next();
            if (item != null && (item.isLive() || item.isLiveReplay())) {
                it.remove();
            }
        }
    }
}
