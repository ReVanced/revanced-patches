package app.revanced.extension.tiktok.feedfilter;

import com.ss.android.ugc.aweme.feed.model.Aweme;
import com.ss.android.ugc.aweme.feed.model.AwemeStatistics;

import app.revanced.extension.tiktok.Utils;
import app.revanced.extension.tiktok.settings.Settings;

public final class LikeCountFilter implements IFilter {

    final long minLike;
    final long maxLike;

    LikeCountFilter() {
        long[] minMax = Utils.parseMinMax(Settings.MIN_MAX_LIKES);
        minLike = minMax[0];
        maxLike = minMax[1];
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public boolean getFiltered(Aweme item) {
        AwemeStatistics statistics = item.getStatistics();
        if (statistics == null) return false;

        long likeCount = statistics.getDiggCount();
        return likeCount < minLike || likeCount > maxLike;
    }
}
