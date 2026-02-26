package app.revanced.extension.tiktok.feedfilter;

import app.revanced.extension.tiktok.settings.Settings;
import com.ss.android.ugc.aweme.feed.model.Aweme;

public class LiveFilter implements IFilter {
    @Override
    public boolean getEnabled() {
        return Settings.HIDE_LIVE.get();
    }

    @Override
    public boolean getFiltered(Aweme item) {
        // TikTok 43.6.2: Aweme no longer exposes isLive(), use liveId/liveType instead.
        return item.getLiveId() != 0 || item.isLiveReplay() || item.getLiveType() != null;
    }
}
