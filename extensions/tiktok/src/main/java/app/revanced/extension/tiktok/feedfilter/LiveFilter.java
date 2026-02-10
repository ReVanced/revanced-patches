package app.revanced.extension.tiktok.feedfilter;

import app.revanced.extension.tiktok.settings.Settings;
import com.ss.android.ugc.aweme.feed.model.Aweme;

public class LiveFilter implements IFilter {
    @Override
    public boolean getEnabled() {
        // HARDCODED: Always filter live streams
        return true;
    }

    @Override
    public boolean getFiltered(Aweme item) {
        return item.getLiveId() != 0 || item.isLiveReplay() || item.getLiveType() != null;
    }
}