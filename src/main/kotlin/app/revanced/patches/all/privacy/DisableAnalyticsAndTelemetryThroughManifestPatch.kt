package app.revanced.patches.all.privacy

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.util.asSequence
import app.revanced.util.getNode
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.util.logging.Logger

@Patch(
    name = "Disable analytics and telemetry through manifest",
    description = "Patches the manifest to disable analytics and telemetry services.",
    use = false,
)
@Suppress("unused")
object DisableAnalyticsAndTelemetryThroughManifestPatch : ResourcePatch() {
    private val logger = Logger.getLogger(this::class.java.name)

    private val disableFirebaseTelemetry by option("Firebase telemetry")
    private val disableFacebookSdk by option("Facebook SDK")
    private val disableGoogleAnalytics by option("Google Analytics")

    override fun execute(context: ResourceContext) {
        context.document["AndroidManifest.xml"].use { document ->
            mapOf(
                disableFirebaseTelemetry to {
                    document.addMetadata(
                        "firebase_analytics_collection_enabled" to "false",
                        "firebase_analytics_collection_deactivated" to "true",
                        "firebase_crashlytics_collection_enabled" to "false",
                        "firebase_performance_collection_enabled" to "false",
                        "firebase_performance_collection_deactivated" to "true",
                        "firebase_data_collection_default_enabled" to "false",
                    )
                },
                disableFacebookSdk to {
                    document.addMetadata(
                        "com.facebook.sdk.AutoLogAppEventsEnabled" to "false",
                        "com.facebook.sdk.AdvertiserIDCollectionEnabled" to "false",
                        "com.facebook.sdk.MonitorEnabled" to "false",
                        "com.facebook.sdk.AutoInitEnabled" to "false",
                    )
                },
                disableGoogleAnalytics to {
                    document.addMetadata(
                        "google_analytics_adid_collection_enabled" to "false",
                        "google_analytics_default_allow_ad_personalization_signals" to "false",
                        "google_analytics_automatic_screen_reporting_enabled" to "false",
                        "google_analytics_default_allow_ad_storage" to "false",
                        "google_analytics_default_allow_ad_user_data" to "false",
                        "google_analytics_default_allow_analytics_storage" to "false",
                        "google_analytics_sgtm_upload_enabled" to "false",
                        "google_analytics_deferred_deep_link_enabled" to "false",
                    )
                },
            ).forEach { option, patch ->
                val isEnabled by option
                if (!isEnabled!!) return@forEach

                val message = try {
                    patch()

                    "Disabled ${option.title}"
                } catch (exception: PatchException) {
                    "${option.title} was not found. Skipping."
                }

                logger.info(message)
            }
        }
    }

    private const val META_DATA_TAG = "meta-data"
    private const val NAME_ATTRIBUTE = "android:name"
    private const val VALUE_ATTRIBUTE = "android:value"

    private fun Document.addMetadata(vararg metaDataNodes: Pair<String, String>) =
        metaDataNodes.forEach { (nodeName, nodeValue) ->
            val applicationNode = getNode("application") as Element
            applicationNode.getElementsByTagName(META_DATA_TAG)
                .asSequence()
                .first { it.attributes.getNamedItem(NAME_ATTRIBUTE).nodeValue == nodeName }
                .attributes.getNamedItem(VALUE_ATTRIBUTE).nodeValue = nodeValue
        }
}
