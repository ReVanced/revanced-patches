package app.revanced.extension.tiktok.feedfilter;

import app.revanced.extension.tiktok.settings.Settings;
import com.ss.android.ugc.aweme.feed.model.Aweme;

public class StoryFilter implements IFilter {
    @Override
    public boolean getEnabled() {
        return Settings.HIDE_STORY.get();
    }

    @Override
    public boolean getFiltered(Aweme item) {
        return item.getIsTikTokStory();
    }
}
