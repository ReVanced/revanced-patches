package app.revanced.tiktok.feedfilter;

import app.revanced.tiktok.settings.SettingsEnum;
import com.ss.android.ugc.aweme.feed.model.Aweme;

public class AdsFilter implements IFilter {
    @Override
    public boolean getEnabled() {
        return SettingsEnum.REMOVE_ADS.getBoolean();
    }

    @Override
    public boolean getFiltered(Aweme item) {
        return item.isAd() || item.isWithPromotionalMusic();
    }
}
