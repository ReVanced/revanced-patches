package app.revanced.extension.tiktok.feedfilter;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.tiktok.settings.Settings;
import com.ss.android.ugc.aweme.feed.model.Aweme;
import com.ss.android.ugc.aweme.feed.model.AwemeStatistics;
import com.ss.android.ugc.aweme.feed.model.FeedItemList;
import com.ss.android.ugc.aweme.follow.presenter.FollowFeedList;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class FeedItemsFilter {
    private static final List<IFilter> FILTERS = List.of(
            new AdsFilter(),
            new LiveFilter(),
            new StoryFilter(),
            new ImageVideoFilter(),
            new ViewCountFilter(),
            new LikeCountFilter(),
            new ShopFilter()
    );

    private static final int MAX_NULL_ITEMS_LOGS = 3;
    private static final AtomicInteger feedItemListNullItemsLogCount = new AtomicInteger();
    private static final AtomicInteger followFeedListNullItemsLogCount = new AtomicInteger();

    public static void filter(FeedItemList feedItemList) {
        boolean verbose = BaseSettings.DEBUG.get();
        if (feedItemList == null || feedItemList.items == null) {
            if (verbose) {
                logNullItems("FeedItemList", feedItemListNullItemsLogCount);
            }
            return;
        }
        if (verbose) {
            debugLogBatch("FeedItemList", feedItemList.items);
        }
        filterFeedList("FeedItemList", feedItemList.items, item -> item, verbose);
    }

    public static void filter(FollowFeedList followFeedList) {
        boolean verbose = BaseSettings.DEBUG.get();
        if (followFeedList == null || followFeedList.mItems == null) {
            if (verbose) {
                logNullItems("FollowFeedList", followFeedListNullItemsLogCount);
            }
            return;
        }
        if (verbose) {
            debugLogBatch("FollowFeedList", followFeedList.mItems);
        }
        filterFeedList("FollowFeedList", followFeedList.mItems, feed -> (feed != null) ? feed.aweme : null, verbose);
    }

    private static <T> void filterFeedList(
            String source,
            List<T> list,
            AwemeExtractor<T> extractor,
            boolean verbose
    ) {
        if (list == null) return;

        // Could be simplified with removeIf() but requires Android 7.0+ while TikTok supports 4.0+.
        int initialSize = list.size();
        int removed = 0;
        Iterator<T> iterator = list.iterator();
        while (iterator.hasNext()) {
            T container = iterator.next();
            Aweme item = extractor.extract(container);
            if (item == null) {
                continue;
            }

            String reason = getFilterReason(item);
            logItem(item, reason, verbose);

            if (reason != null) {
                removed++;
                iterator.remove();
            }
        }

        if (verbose) {
            final int removedFinal = removed;
            Logger.printInfo(() -> "[ReVanced FeedFilter] filter(" + source + "): size " + initialSize + " -> " + list.size()
                    + " (removed=" + removedFinal + ", verbose=" + verbose + ")");
        }
    }

    private static boolean shouldFilter(Aweme item) {
        return getFilterReason(item) != null;
    }

    private static String getFilterReason(Aweme item) {
        for (IFilter filter : FILTERS) {
            if (filter.getEnabled() && filter.getFiltered(item)) {
                return filter.getClass().getSimpleName();
            }
        }
        return null;
    }

    private static void logNullItems(String source, AtomicInteger counter) {
        int count = counter.getAndIncrement();
        if (count < MAX_NULL_ITEMS_LOGS) {
            Logger.printInfo(() -> "[ReVanced FeedFilter] filter(" + source + "): items=null");
        } else if (count == MAX_NULL_ITEMS_LOGS) {
            Logger.printInfo(() -> "[ReVanced FeedFilter] filter(" + source + "): items=null (further logs suppressed)");
        }
    }

    private static <T> void debugLogBatch(String source, List<T> list) {
        int size = list == null ? -1 : list.size();
        Logger.printInfo(() ->
                "[ReVanced FeedFilter] filter(" + source + "): size=" + size
                        + " remove_ads=" + Settings.REMOVE_ADS.get()
                        + " hide_shop=" + Settings.HIDE_SHOP.get()
                        + " hide_live=" + Settings.HIDE_LIVE.get()
                        + " hide_story=" + Settings.HIDE_STORY.get()
                        + " hide_image=" + Settings.HIDE_IMAGE.get()
                        + " min_max_views=\"" + Settings.MIN_MAX_VIEWS.get() + "\""
                        + " min_max_likes=\"" + Settings.MIN_MAX_LIKES.get() + "\""
        );
    }

    private static void logItem(Aweme item, String reason, boolean verbose) {
        if (!verbose) return;

        String shareUrl = item.getShareUrl();
        if (shareUrl != null && shareUrl.length() > 140) {
            shareUrl = shareUrl.substring(0, 140) + "...";
        }

        String finalShareUrl = shareUrl;
        Logger.printInfo(() -> {
            long playCount = -1;
            long likeCount = -1;
            AwemeStatistics statistics = item.getStatistics();
            if (statistics != null) {
                playCount = statistics.getPlayCount();
                likeCount = statistics.getDiggCount();
            }

            var imageInfos = item.getImageInfos();
            boolean isImage = imageInfos != null && !imageInfos.isEmpty();
            boolean isPhotoMode = item.getPhotoModeImageInfo() != null || item.getPhotoModeTextInfo() != null;
            boolean isLive = item.getLiveId() != 0 || item.getLiveType() != null;

            return "[ReVanced FeedFilter] item"
                    + " aid=" + item.getAid()
                    + " ad=" + item.isAd()
                    + " promo=" + item.isWithPromotionalMusic()
                    + " live=" + isLive
                    + " liveReplay=" + item.isLiveReplay()
                    + " story=" + item.getIsTikTokStory()
                    + " image=" + isImage
                    + " photoMode=" + isPhotoMode
                    + " playCount=" + playCount
                    + " likeCount=" + likeCount
                    + " shareUrl=" + (finalShareUrl == null ? "null" : "\"" + finalShareUrl + "\"")
                    + " => " + (reason == null ? "KEEP" : "FILTER(" + reason + ")");
        });
    }

    @FunctionalInterface
    interface AwemeExtractor<T> {
        Aweme extract(T source);
    }
}
