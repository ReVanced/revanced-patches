package app.revanced.extension.tiktok.feedfilter;

import app.revanced.extension.tiktok.settings.Settings;
import com.ss.android.ugc.aweme.feed.model.Aweme;

public class ShopFilter implements IFilter {
    @Override
    public boolean getEnabled() {
        return true;
        // return Settings.HIDE_SHOP.get();
    }

    @Override
    public boolean getFiltered(Aweme item) {
        if (item == null) return false;

        // Attached Products (TikTok Shop)
        if (item.productsInfo != null && !item.productsInfo.isEmpty()) {
            return true;
        }

        // Simple Promotions (Banner links)
        if (item.simplePromotions != null && !item.simplePromotions.isEmpty()) {
            return true;
        }

        // Shop Ads
        if (item.shopAdStruct != null) {
            return true;
        }

        // Fallback (URL check)
        String shareUrl = item.getShareUrl();
        return shareUrl != null && shareUrl.contains("placeholder_product_id");
    }
}
