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
        // TikTok 43.6.2: Aweme no longer exposes isImage()/isPhotoMode().
        var imageInfos = item.getImageInfos();
        boolean isImage = imageInfos != null && !imageInfos.isEmpty();
        boolean isPhotoMode = item.getPhotoModeImageInfo() != null || item.getPhotoModeTextInfo() != null;
        return isImage || isPhotoMode;
    }
}
