package app.revanced.extension.tiktok.feedfilter;

import app.revanced.extension.tiktok.settings.Settings;
import com.ss.android.ugc.aweme.feed.model.Aweme;
import com.ss.android.ugc.aweme.commerce.AwemeCommerceStruct;

public class AdsFilter implements IFilter {
    @Override
    public boolean getEnabled() {
        return true;
        // return Settings.REMOVE_ADS.get();
    }

    @Override
    public boolean getFiltered(Aweme item) {
        try {
            // Standard Ads & Promotional Music
            if (item.isAd() || item.isWithPromotionalMusic()) {
                return true;
            }

            // Paid Partnerships (Branded Content)
            if (item.mCommerceVideoAuthInfo != null) {
                if (item.mCommerceVideoAuthInfo.isBrandedContent()) {
                    return true;
                }
            }
        } catch (Throwable t) {
            return false;
        }
        return false;
    }
}
