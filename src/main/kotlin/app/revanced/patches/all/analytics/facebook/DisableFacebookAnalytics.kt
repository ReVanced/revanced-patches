package app.revanced.patches.all.analytics.facebook

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.resource.AndroidManifest

@Patch(
    name = "Disable Facebook Analytics",
    description = "Disables parts of the Facebook SDK responsible for data gathering.",
    use = false,
)
@Suppress("unused")
object DisableFacebookAnalytics : ResourcePatch() {
    override fun execute(context: ResourceContext) {
        mapOf(
            "com.facebook.sdk.AutoLogAppEventsEnabled" to "false",
            "com.facebook.sdk.AdvertiserIDCollectionEnabled" to "false",
            "com.facebook.sdk.MonitorEnabled" to "false",
            // This entry disables completely the SDK, preventing Facebook login from working, may not be desired
            //"com.facebook.sdk.AutoInitEnabled" to "false"
            // TODO Add a patch option to choose to disable the SDK completely ?
        ).forEach {
            AndroidManifest.addMetadata(context, it.key, it.value)
        }
    }
}