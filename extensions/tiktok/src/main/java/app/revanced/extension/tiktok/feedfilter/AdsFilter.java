package app.revanced.extension.tiktok.feedfilter;

import app.revanced.extension.tiktok.settings.Settings;
import com.ss.android.ugc.aweme.feed.model.Aweme;

public class AdsFilter implements IFilter {
    @Override
    public boolean getEnabled() {
        return true;
        // return Settings.REMOVE_ADS.get();
    }

    @Override
    public boolean getFiltered(Aweme item) {
        if (item == null) return false;

        // TikTok's Internal Commercial Types
        // Verified in AwemeExtKt: 1, 29, 30, 32, 33, 201 are commercial
        int type = item.getAwemeType();
        if (type == 1 || type == 29 || type == 30 || type == 32 || type == 33 || type == 201) {
            return true;
        }

        // Ad Flags (Hard and Soft/Sponsored)
        if (item.isAd || item.isSoftAd || item.awemeRawAd != null) {
            return true;
        }

        // Music Marketing
        if (item.isWithPromotionalMusic()) return true;

        if (item.mCommerceVideoAuthInfo != null) {
            // PseudoAds (Spark Ads) and Branded Content
            return item.mCommerceVideoAuthInfo.isBrandedContent() || 
                   item.mCommerceVideoAuthInfo.isPseudoAd();
        }

        return false;
    }
}
