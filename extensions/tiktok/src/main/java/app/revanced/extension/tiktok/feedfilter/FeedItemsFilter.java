package app.revanced.extension.tiktok.feedfilter;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.settings.BaseSettings;
import com.ss.android.ugc.aweme.feed.model.Aweme;
import com.ss.android.ugc.aweme.feed.model.FeedItemList;
import com.ss.android.ugc.aweme.follow.presenter.FollowFeedList;

import java.util.Iterator;
import java.util.List;

public final class FeedItemsFilter {
    public static void filter(FeedItemList feedItemList) {
        boolean verbose = BaseSettings.DEBUG.get();
        if (feedItemList == null || feedItemList.items == null) return;

        filterFeedList("FeedItemList", feedItemList.items, item -> item, verbose);
    }

    public static void filter(FollowFeedList followFeedList) {
        boolean verbose = BaseSettings.DEBUG.get();
        if (followFeedList == null || followFeedList.mItems == null) return;

        filterFeedList("FollowFeedList", followFeedList.mItems, feed -> (feed != null) ? feed.aweme : null, verbose);
    }

    private static <T> void filterFeedList(
            String source,
            List<T> list,
            AwemeExtractor<T> extractor,
            boolean verbose
    ) {
        if (list == null) return;

        int initialSize = list.size();
        int removed = 0;
        Iterator<T> iterator = list.iterator();

        while (iterator.hasNext()) {
            T container = iterator.next();
            Aweme item = extractor.extract(container);
            if (item == null) continue;

            String reason = getInternalizedFilterReason(item);

            if (reason != null) {
                removed++;
                iterator.remove();
                if (verbose) {
                    logItem(item, reason);
                }
            }
        }

        if (verbose && removed > 0) {
            int finalRemoved = removed;
            Logger.printInfo(() -> "[ReVanced FeedFilter] " + source + ": removed " + finalRemoved + " items.");
        }
    }

    private static String getInternalizedFilterReason(Aweme item) {
        if (item.isAd() || item.isWithPromotionalMusic()) {
            return "AdsFilter";
        }

        if (item.getLiveId() != 0 || item.getLiveType() != null || item.isLiveReplay()) {
            return "LiveFilter";
        }

        String shareUrl = item.getShareUrl();
        if (shareUrl != null && shareUrl.contains("placeholder_product_id")) {
            return "ShopFilter";
        }

        return null;
    }

    private static void logItem(Aweme item, String reason) {
        Logger.printInfo(() -> "[ReVanced FeedFilter] FILTERED: aid=" + item.getAid() + " Reason=" + reason);
    }

    @FunctionalInterface
    interface AwemeExtractor<T> {
        Aweme extract(T source);
    }
}