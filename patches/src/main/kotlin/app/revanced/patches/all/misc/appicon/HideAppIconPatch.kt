package app.revanced.patches.all.misc.appicon

import app.revanced.patcher.patch.resourcePatch
import org.w3c.dom.Element
import java.util.logging.Logger

@Suppress("unused")
val hideAppIconPatch = resourcePatch(
    name = "Hide app icon",
    description = "Hides the app icon from the Android launcher.",
    use = false,
) {
    execute {
        document("AndroidManifest.xml").use { document ->
            val intentFilters = document.getElementsByTagName("intent-filter")
            var modified = false

            for (i in 0 until intentFilters.length) {
                val node = intentFilters.item(i) as? Element ?: continue
                var hasMainAction = false
                var launcherCategory: Element? = null

                val nodeChildren = node.childNodes
                for (j in 0 until nodeChildren.length) {
                    val child = nodeChildren.item(j) as? Element ?: continue
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

                if (hasMainAction && launcherCategory != null) {
                    launcherCategory.setAttribute("android:name", "android.intent.category.DEFAULT")
                    modified = true
                }
            }

            if (!modified) {
                Logger.getLogger(this::class.java.name).warning(
                    "Did not find any launcher intent-filters to modify. No changes applied."
                )
            }
        }
    }
}


