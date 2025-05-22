package app.revanced.patches.all.misc.appicon

import app.revanced.patcher.patch.resourcePatch
import org.w3c.dom.Element
import java.util.logging.Logger

private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"

@Suppress("unused")
val hideAppIconPatch = resourcePatch(
    name = "Hide app icon",
    description = "Hides the app icon from the launcher using intent-filter changes and activity modifications.",
    use = false,
) {
    execute {
        document("AndroidManifest.xml").use { document ->
            val logger = Logger.getLogger("hideAppIconPatch")

            var modified = false

            // **Method 1:** Modify intent-filter category from LAUNCHER to DEFAULT
            val categoryNodes = document.getElementsByTagName("category")
            for (i in 0 until categoryNodes.length) {
                val category = categoryNodes.item(i) as? Element ?: continue
                val categoryName = category.getAttributeNS(ANDROID_NS, "name")

                if (category.hasAttributeNS(ANDROID_NS, "name") && categoryName == "android.intent.category.LAUNCHER") {
                    category.setAttributeNS(ANDROID_NS, "name", "android.intent.category.DEFAULT")
                    modified = true
                }
            }

            // **Method 2:** Disable launcher activities by modifying android:enabled and android:exported
            val activityNodes = document.getElementsByTagName("activity")
            for (i in 0 until activityNodes.length) {
                val activity = activityNodes.item(i) as? Element ?: continue
                val activityName = activity.getAttributeNS(ANDROID_NS, "name")

                if (activityName.contains("Launcher", ignoreCase = true)) {
                    activity.setAttributeNS(ANDROID_NS, "enabled", "false")
                    activity.setAttributeNS(ANDROID_NS, "exported", "false")
                    modified = true
                }
            }

            if (!modified) {
                logger.warning("No launcher activities or intent-filter modifications were made—app icon may still be visible.")
            }
        }
    }
}