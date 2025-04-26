package app.revanced.extension.tiktok.feedfilter;

import com.ss.android.ugc.aweme.feed.model.Aweme;
import com.ss.android.ugc.aweme.feed.model.FeedItemList;
import com.ss.android.ugc.aweme.follow.presenter.FollowFeedList;

import java.util.Iterator;
import java.util.List;

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

    public static void filter(FeedItemList feedItemList) {
        filterFeedList(feedItemList.items, item -> item);
    }

    public static void filter(FollowFeedList followFeedList) {
        filterFeedList(followFeedList.mItems, feed -> (feed != null) ? feed.aweme : null);
    }

    private static <T> void filterFeedList(List<T> list, AwemeExtractor<T> extractor) {
        // Could be simplified with removeIf() but requires Android 7.0+ while TikTok supports 4.0+.
        Iterator<T> iterator = list.iterator();
        while (iterator.hasNext()) {
            T container = iterator.next();
            Aweme item = extractor.extract(container);
            if (item != null && shouldFilter(item)) {
                iterator.remove();
            }
        }
    }

    private static boolean shouldFilter(Aweme item) {
        for (IFilter filter : FILTERS) {
            if (filter.getEnabled() && filter.getFiltered(item)) {
                return true;
            }
        }
        return false;
    }

    @FunctionalInterface
    interface AwemeExtractor<T> {
        Aweme extract(T source);
    }
}