package app.revanced.extension.tiktok.feedfilter;

import app.revanced.extension.tiktok.settings.Settings;
import com.ss.android.ugc.aweme.feed.model.Aweme;

public class ImageVideoFilter implements IFilter {
    @Override
    public boolean getEnabled() {
        return Settings.HIDE_IMAGE.get();
    }

    @Override
    public boolean getFiltered(Aweme item) {
        if (item == null) return false;

        int type = item.getAwemeType();

        // 2 = Standard Image, 150 = Photo Mode, 160 = Text Mode
        if (type == 2 || type == 150 || type == 160) {
            return true;
        }

        // Fallback checks
        var imageInfos = item.getImageInfos();
        return imageInfos != null && !imageInfos.isEmpty();
    }
}
