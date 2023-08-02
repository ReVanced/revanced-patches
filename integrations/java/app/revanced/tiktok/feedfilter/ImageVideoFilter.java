package app.revanced.tiktok.feedfilter;

import app.revanced.tiktok.settings.SettingsEnum;
import com.ss.android.ugc.aweme.feed.model.Aweme;

public class ImageVideoFilter implements IFilter {
    @Override
    public boolean getEnabled() {
        return SettingsEnum.HIDE_IMAGE.getBoolean();
    }

    @Override
    public boolean getFiltered(Aweme item) {
        return item.isImage() || item.isPhotoMode();
    }
}
