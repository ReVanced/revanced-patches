package app.revanced.patches.all.privacy

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.booleanPatchOption
import app.revanced.patches.shared.resource.AndroidManifest
import java.util.logging.Logger

@Patch(
    name = "Disable more privacy invasive components",
    description = "Disables multiple embedded analytics and telemetry SDKs by modifying the app's manifest. Please note this can break some apps.",
    use = false
)
@Suppress("unused")
object UniversalResourcePrivacyPatch : ResourcePatch() {

    private val subPatchesOptions = mapOf(
        ::disableFirebaseCollections to booleanPatchOption(
            key = "disableFirebaseCollections",
            default = true,
            values = mapOf(),
            title = "Firebase collections",
            description = "Disables multiple Firebase data collection mechanisms.",
            required = true
        ),
        ::disableFacebookAnalytics to booleanPatchOption(
            key = "disableFacebookAnalytics",
            default = true,
            values = mapOf(),
            title = "Facebook Analytics",
            description = "Disables parts of the Facebook SDK responsible for data gathering.",
            required = true
        ),
        ::disableFacebookSDK to booleanPatchOption(
            key = "disableFacebookSDK",
            default = false,
            values = mapOf(),
            title = "Facebook SDK",
            description = "Disables the Facebook SDK. Will break Facebook login.",
            required = true
        ),
        ::disableGoogleAnalyticsCollections to booleanPatchOption(
            key = "Google Analytics collections",
            default = true,
            values = mapOf(),
            title = "Apps Flyer",
            description = "Disables multiple Google Analytics data collection mechanisms.",
            required = true
        ),
    )

    private fun disableFacebookAnalytics(context: ResourceContext) {
        mapOf(
            "com.facebook.sdk.AutoLogAppEventsEnabled" to "false",
            "com.facebook.sdk.AdvertiserIDCollectionEnabled" to "false",
            "com.facebook.sdk.MonitorEnabled" to "false"
        ).forEach {
            AndroidManifest.addMetadata(context, it.key, it.value)
        }
    }

    private fun disableFacebookSDK(context: ResourceContext) {
        AndroidManifest.addMetadata(context, "com.facebook.sdk.AutoInitEnabled", "false")
    }

    private fun disableFirebaseCollections(context: ResourceContext) {
        mapOf(
            "firebase_analytics_collection_enabled" to "false",
            "firebase_analytics_collection_deactivated" to "true",
            "firebase_crashlytics_collection_enabled" to "false",
            "firebase_performance_collection_enabled" to "false",
            "firebase_performance_collection_deactivated" to "true",
            "firebase_data_collection_default_enabled" to "false"
        ).forEach {
            AndroidManifest.addMetadata(context, it.key, it.value)
        }
    }

    private fun disableGoogleAnalyticsCollections(context: ResourceContext) {
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

    override fun execute(context: ResourceContext) {
        subPatchesOptions.forEach {
            if (it.value.value == true){
                try {
                    it.key(context)
                    Logger.getLogger(this::class.java.name).info("Applied privacy patch to disable ${it.value.title}")
                }catch (exception: PatchException){
                    Logger.getLogger(this::class.java.name).info("${it.value.title} not found, skipping")
                }
            }
        }

    }
}