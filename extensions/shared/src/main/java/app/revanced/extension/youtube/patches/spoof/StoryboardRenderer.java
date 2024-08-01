package app.revanced.extension.youtube.patches.spoof;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

@Deprecated
public final class StoryboardRenderer {
    public final String videoId;
    @Nullable
    public final String spec;
    public final boolean isLiveStream;
    /**
     * Recommended image quality level, or NULL if no recommendation exists.
     */
    @Nullable
    public final Integer recommendedLevel;

    public StoryboardRenderer(String videoId, @Nullable String spec, boolean isLiveStream, @Nullable Integer recommendedLevel) {
        this.videoId = videoId;
        this.spec = spec;
        this.isLiveStream = isLiveStream;
        this.recommendedLevel = recommendedLevel;
    }

    @NotNull
    @Override
    public String toString() {
        return "StoryboardRenderer{" +
                "videoId=" + videoId +
                ", isLiveStream=" + isLiveStream +
                ", spec='" + spec + '\'' +
                ", recommendedLevel=" + recommendedLevel +
                '}';
    }
}
