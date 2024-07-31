package app.revanced.patches.tumblr.ads

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.tumblr.timelinefilter.addTimelineObjectTypeFilter
import app.revanced.patches.tumblr.timelinefilter.filterTimelineObjectsPatch

@Suppress("unused")
val disableDashboardAdsPatch = bytecodePatch(
    name = "Disable dashboard ads",
    description = "Disables ads in the dashboard.",
) {
    dependsOn(filterTimelineObjectsPatch)

    compatibleWith("com.tumblr")

    execute {
        // The timeline object types are filtered by their name in the TimelineObjectType enum.
        // This is often different from the "object_type" returned in the api (noted in comments here)
        arrayOf(
            "CLIENT_SIDE_MEDIATION", // "client_side_ad_waterfall"
            "GEMINI_AD", // "backfill_ad"

            // The object types below weren't actually spotted in the wild in testing, but they are valid Object types
            // and their names clearly indicate that they are ads, so we just block them anyway,
            // just in case they will be used in the future.
            "NIMBUS_AD", // "nimbus_ad"
            "CLIENT_SIDE_AD", // "client_side_ad"
            "DISPLAY_IO_INTERSCROLLER_AD", // "display_io_interscroller"
            "DISPLAY_IO_HEADLINE_VIDEO_AD", // "display_io_headline_video"
            "FACEBOOK_BIDDAABLE", // "facebook_biddable_sdk_ad"
            "GOOGLE_NATIVE", // "google_native_ad"
        ).forEach {
            addTimelineObjectTypeFilter(it)
        }
    }
}
