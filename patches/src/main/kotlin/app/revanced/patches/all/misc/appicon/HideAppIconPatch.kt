package app.revanced.patches.all.misc.appicon

import app.revanced.patcher.patch.creatingResourcePatch
import app.revanced.util.asSequence
import app.revanced.util.childElementsSequence
import org.w3c.dom.Element
import java.util.logging.Logger

@Suppress("unused", "ObjectPropertyName")
val `Hide app icon` by creatingResourcePatch(
    description = "Hides the app icon from the Android launcher.",
    use = false,
) {
    apply {
        document("AndroidManifest.xml").use { document ->
            var changed = false

            val intentFilters = document.getElementsByTagName("intent-filter")
            for (node in intentFilters.asSequence().filterIsInstance<Element>()) {
                var hasMainAction = false
                var launcherCategory: Element? = null

                for (child in node.childElementsSequence()) {
                    when (child.tagName) {
                        "action" -> if (child.getAttribute("android:name") == "android.intent.action.MAIN") {
                            hasMainAction = true
                        }

                        "category" -> if (child.getAttribute("android:name") == "android.intent.category.LAUNCHER") {
                            launcherCategory = child
                        }
                    }
                }

                if (hasMainAction && launcherCategory != null) {
                    launcherCategory.setAttribute("android:name", "android.intent.category.DEFAULT")
                    changed = true
                }
            }

            if (!changed) {
                Logger.getLogger(this::class.java.name)
                    .warning("No changes made: Did not find any launcher intent-filters to change.")
            }
        }
    }
}

