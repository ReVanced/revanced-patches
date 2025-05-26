package app.revanced.patches.all.misc.appicon

import app.revanced.patcher.patch.resourcePatch
import org.w3c.dom.Element
import java.util.logging.Logger

@Suppress("unused")
val hideAppIconPatch = resourcePatch(
    name = "Hide app icon",
    description = "Hides the app icon from the launcher by modifying all launcher intent filters in the manifest.",
    use = false,
) {
    execute {
        document("AndroidManifest.xml").use { document ->
            val logger = Logger.getLogger("HideAppIconPatch")
            val intentFilters = document.getElementsByTagName("intent-filter")
            var modified = false

            for (i in 0 until intentFilters.length) {
                val node = intentFilters.item(i)
                if (node is Element) {
                    val filter = node

                    var hasMainAction = false
                    var launcherCategory: Element? = null

                    for (j in 0 until filter.childNodes.length) {
                        val child = filter.childNodes.item(j)
                        if (child is Element) {
                            when (child.tagName) {
                                "action" -> {
                                    if (child.getAttribute("android:name") == "android.intent.action.MAIN") {
                                        hasMainAction = true
                                    }
                                }
                                "category" -> {
                                    if (child.getAttribute("android:name") == "android.intent.category.LAUNCHER") {
                                        launcherCategory = child
                                    }
                                }
                            }
                        }
                    }

                    if (hasMainAction && launcherCategory != null) {
                        launcherCategory.setAttribute("android:name", "android.intent.category.DEFAULT")
                        modified = true
                    }
                }
            }

            if (!modified) {
                logger.warning("No launcher intent-filters found to modify.")
            }
        }
    }
}


