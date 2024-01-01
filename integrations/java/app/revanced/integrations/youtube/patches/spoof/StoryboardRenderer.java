package app.revanced.integrations.youtube.patches.spoof;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

public final class StoryboardRenderer {
    @Nullable
    private final String spec;
    private final boolean isLiveStream;
    @Nullable
    private final Integer recommendedLevel;

    public StoryboardRenderer(@Nullable String spec, boolean isLiveStream, @Nullable Integer recommendedLevel) {
        this.spec = spec;
        this.isLiveStream = isLiveStream;
        this.recommendedLevel = recommendedLevel;
    }

    @Nullable
    public String getSpec() {
        return spec;
    }

    public boolean isLiveStream() {
        return isLiveStream;
    }

    /**
     * @return Recommended image quality level, or NULL if no recommendation exists.
     */
    @Nullable
    public Integer getRecommendedLevel() {
        return recommendedLevel;
    }

    @NotNull
    @Override
    public String toString() {
        return "StoryboardRenderer{" +
                "isLiveStream=" + isLiveStream +
                ", spec='" + spec + '\'' +
                ", recommendedLevel=" + recommendedLevel +
                '}';
    }
}
