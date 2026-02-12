package app.revanced.extension.tiktok.feedfilter;

import app.revanced.extension.tiktok.settings.Settings;
import com.ss.android.ugc.aweme.feed.model.Aweme;

public class StoryFilter implements IFilter {
    @Override
    public boolean getEnabled() {
        return Settings.HIDE_STORY.get();
    }

    public boolean getFiltered(Aweme item) {
        if (item == null) return false;

        if (item.isTikTokStory) return true;
        
        // Type 40 = Standard Story, 11 = Legacy/Region Story
        int type = item.getAwemeType();
        if (type == 40 || type == 11 || item.isTikTokStory) {
            return true;
        }

        return false;
    }
}
