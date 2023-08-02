package app.revanced.tiktok.feedfilter;

import app.revanced.tiktok.settings.SettingsEnum;
import com.ss.android.ugc.aweme.feed.model.Aweme;

public class LiveFilter implements IFilter {
    @Override
    public boolean getEnabled() {
        return SettingsEnum.HIDE_LIVE.getBoolean();
    }

    @Override
    public boolean getFiltered(Aweme item) {
        return item.isLive() || item.isLiveReplay();
    }
}
