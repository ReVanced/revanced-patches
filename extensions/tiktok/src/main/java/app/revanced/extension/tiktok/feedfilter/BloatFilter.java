package app.revanced.extension.tiktok.feedfilter;

import com.ss.android.ugc.aweme.feed.model.Aweme;

public class BloatFilter implements IFilter {
    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public boolean getFiltered(Aweme item) {
        if (item == null) return false;

        // Full screen promos
        if (item.isReferralFakeAweme || item.isRecBigCardFakeAweme) {
            return true;
        }

        // System cards (non video interrupts)
        if (item.awemeType == 104 || item.awemeType == 105) return true;

        // Accounts to follow recs and overlays
        if (item.recommendCardType != 0) return true;

        return false;
    }
}