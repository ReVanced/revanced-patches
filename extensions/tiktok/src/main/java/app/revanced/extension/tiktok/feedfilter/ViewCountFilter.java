package app.revanced.extension.tiktok.feedfilter;

import app.revanced.extension.tiktok.Utils;
import app.revanced.extension.tiktok.settings.Settings;
import com.ss.android.ugc.aweme.feed.model.Aweme;
import com.ss.android.ugc.aweme.feed.model.AwemeStatistics;


public class ViewCountFilter implements IFilter {
    final long minView;
    final long maxView;

    ViewCountFilter() {
        long[] minMax = Utils.parseMinMax(Settings.MIN_MAX_VIEWS);
        minView = minMax[0];
        maxView = minMax[1];
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public boolean getFiltered(Aweme item) {
        AwemeStatistics statistics = item.getStatistics();
        if (statistics == null) return false;

        long playCount = statistics.getPlayCount();
        return playCount < minView || playCount > maxView;
    }
}
