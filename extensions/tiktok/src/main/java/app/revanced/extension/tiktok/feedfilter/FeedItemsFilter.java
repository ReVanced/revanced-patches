package app.revanced.extension.tiktok.feedfilter;

import com.ss.android.ugc.aweme.feed.model.Aweme;
import com.ss.android.ugc.aweme.feed.model.FeedItemList;
import com.ss.android.ugc.aweme.follow.presenter.FollowFeedList;

import java.util.Iterator;
import java.util.List;

public final class FeedItemsFilter {

    private static final IFilter[] FILTERS = new IFilter[] {
        new AdsFilter(),
        new LiveFilter(),
        new ShopFilter(),
        new StoryFilter(),
        new ImageVideoFilter(),
        new BloatFilter()
    };

    public static void filter(FeedItemList feedItemList) {
        if (feedItemList == null || feedItemList.items == null) return;
        filterFeedList(feedItemList.items, item -> item);
    }

    public static void filter(FollowFeedList followFeedList) {
        if (followFeedList == null || followFeedList.mItems == null) return;
        filterFeedList(followFeedList.mItems, feed -> (feed != null) ? feed.aweme : null);
    }

    private static <T> void filterFeedList(
            List<T> list,
            AwemeExtractor<T> extractor
    ) {
        if (list == null) return;

        Iterator<T> iterator = list.iterator();

        while (iterator.hasNext()) {
            T container = iterator.next();
            Aweme item = extractor.extract(container);

            if (item == null) continue;

            if (shouldFilter(item)) {
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