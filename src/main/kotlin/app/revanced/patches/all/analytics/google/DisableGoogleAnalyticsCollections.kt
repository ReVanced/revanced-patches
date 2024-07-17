package app.revanced.patches.all.analytics.google

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.resource.AndroidManifest

@Patch(
    name = "Disable Google Analytics collections",
    description = "Disables multiple Google Analytics data collection mechanisms.",
    use = false,
)
@Suppress("unused")
object DisableGoogleAnalyticsCollections : ResourcePatch() {
    override fun execute(context: ResourceContext) {
        mapOf(
            "google_analytics_adid_collection_enabled" to "false",
            "google_analytics_default_allow_ad_personalization_signals" to "false",
            "google_analytics_automatic_screen_reporting_enabled" to "false",
            "google_analytics_default_allow_ad_storage" to "false",
            "google_analytics_default_allow_ad_user_data" to "false",
            "google_analytics_default_allow_analytics_storage" to "false",
            "google_analytics_sgtm_upload_enabled" to "false",
            "google_analytics_deferred_deep_link_enabled" to "false"
        ).forEach {
            AndroidManifest.addMetadata(context, it.key, it.value)
        }
    }
}