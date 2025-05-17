package app.revanced.patches.all.misc.appicon

import app.revanced.patcher.patch.resourcePatch
import org.w3c.dom.Element
import java.util.logging.Logger

private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"

@Suppress("unused")
val hideAppIconPatch = resourcePatch(
    name = "Hide app icon",
    description = "Hides the app icon from the launcher by changing the intent filter category.",
    use = false,
) {
    execute {
        document("AndroidManifest.xml").use { document ->
            val logger = Logger.getLogger("hideAppIconPatch")

            val categoryNodes = document.getElementsByTagName("category")
            if (categoryNodes.length == 0) {
                logger.warning("No <category> elements found in AndroidManifest.xml. Skipping modification.")
                return@execute
            }

            val validCategoryNodes = (0 until categoryNodes.length)
                .mapNotNull { categoryNodes.item(it) as? Element }
                .filter { (it.parentNode as? Element)?.nodeName == "intent-filter" }

            if (validCategoryNodes.isEmpty()) {
                logger.warning("No valid 'android.intent.category.LAUNCHER' found in intent-filter blocks. Skipping modification.")
                return@execute
            }

            var modified = false
            for (category in validCategoryNodes) {
                val categoryName = category.getAttributeNS(ANDROID_NS, "name")

                if (category.hasAttributeNS(ANDROID_NS, "name") && categoryName == "android.intent.category.LAUNCHER") {
                    category.setAttributeNS(ANDROID_NS, "name", "android.intent.category.DEFAULT")
                    modified = true
                }
            }

            if (!modified) {
                logger.warning("No 'android.intent.category.LAUNCHER' found or already set to DEFAULT—no modifications made.")
            }
        }
    }
}