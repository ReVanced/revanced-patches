package app.revanced.integrations.tiktok.feedfilter;

import com.ss.android.ugc.aweme.feed.model.Aweme;

public interface IFilter {
    boolean getEnabled();

    boolean getFiltered(Aweme item);
}
