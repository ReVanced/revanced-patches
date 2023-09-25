package app.revanced.integrations.patches.spoof;

import androidx.annotation.NonNull;
import org.jetbrains.annotations.NotNull;

public final class StoryboardRenderer {
    private final String spec;
    private final int recommendedLevel;

    public StoryboardRenderer(String spec, int recommendedLevel) {
        this.spec = spec;
        this.recommendedLevel = recommendedLevel;
    }

    @NonNull
    public String getSpec() {
        return spec;
    }

    public int getRecommendedLevel() {
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
