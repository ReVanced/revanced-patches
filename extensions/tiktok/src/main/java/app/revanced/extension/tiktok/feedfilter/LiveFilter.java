package app.revanced.extension.tiktok.feedfilter;

import app.revanced.extension.tiktok.settings.Settings;
import com.ss.android.ugc.aweme.feed.model.Aweme;

public class LiveFilter implements IFilter {
    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public boolean getFiltered(Aweme item) {
        if (item == null) return false;

        // awemeType 101 is the 'isLive' check in code
        if (item.getAwemeType() == 101 || item.getRoom() != null) {
            return true;
        }

        // Fallbacks
        return item.isLiveReplay() || item.getLiveId() != 0 || item.getLiveType() != null;
    }
}