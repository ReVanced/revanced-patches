package app.revanced.integrations.patches.spoof;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

public final class StoryboardRenderer {
    private final String spec;
    @Nullable
    private final Integer recommendedLevel;

    public StoryboardRenderer(String spec, @Nullable Integer recommendedLevel) {
        this.spec = spec;
        this.recommendedLevel = recommendedLevel;
    }

    @NonNull
    public String getSpec() {
        return spec;
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
                "spec='" + spec + '\'' +
                ", recommendedLevel=" + recommendedLevel +
                '}';
    }
}
