package app.revanced.patches.all.analytics.firebase

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.resource.AndroidManifest

@Patch(
    name = "Disable Firebase collections",
    description = "Disables multiple Firebase data collection mechanisms."
)
@Suppress("unused")
object DisableFirebaseCollections : ResourcePatch() {
    override fun execute(context: ResourceContext) {
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
}