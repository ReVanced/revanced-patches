package app.revanced.patches.tumblr.live

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.tumblr.featureflags.addFeatureFlagOverride
import app.revanced.patches.tumblr.featureflags.overrideFeatureFlagsPatch
import app.revanced.patches.tumblr.timelinefilter.addTimelineObjectTypeFilter
import app.revanced.patches.tumblr.timelinefilter.filterTimelineObjectsPatch

@Suppress("unused")
@Deprecated("Tumblr Live was removed and is no longer served in the feed, making this patch useless.")
val disableTumblrLivePatch = bytecodePatch(
    description = "Disable the Tumblr Live tab button and dashboard carousel.",
) {
    dependsOn(
        overrideFeatureFlagsPatch,
        filterTimelineObjectsPatch,
    )

    compatibleWith("com.tumblr")

    execute {
        // Hide the LIVE_MARQUEE timeline element that appears in the feed
        // Called "live_marquee" in api response
        addTimelineObjectTypeFilter("LIVE_MARQUEE")

        // Hide the Tab button for Tumblr Live by forcing the feature flag to false
        addFeatureFlagOverride("liveStreaming", "false")
    }
}
