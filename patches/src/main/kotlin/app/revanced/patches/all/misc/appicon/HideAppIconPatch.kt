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
            val manifest = document.getElementsByTagName("manifest").item(0) as? Element ?: return@use
            val application = document.getElementsByTagName("application").item(0) as? Element ?: return@use

            val activities = document.getElementsByTagName("activity")
            var mainActivityName: String? = null

            for (i in 0 until activities.length) {
                val activity = activities.item(i) as? Element ?: continue
                val intentFilters = activity.getElementsByTagName("intent-filter")

                for (j in 0 until intentFilters.length) {
                    val filter = intentFilters.item(j) as? Element ?: continue
                    var hasMain = false
                    var hasLauncher = false

                    val children = filter.childNodes
                    for (k in 0 until children.length) {
                        val child = children.item(k) as? Element ?: continue
                        when (child.tagName) {
                            "action" -> if (child.getAttribute("android:name") == "android.intent.action.MAIN") hasMain = true
                            "category" -> if (child.getAttribute("android:name") == "android.intent.category.LAUNCHER") hasLauncher = true
                        }
                    }

                    if (hasMain && hasLauncher) {
                        mainActivityName = activity.getAttribute("android:name")
                        break
                    }
                }

                if (mainActivityName != null) break
            }

            if (mainActivityName == null) {
                Logger.getLogger(this::class.java.name).warning("No main launcher activity found.")
                return@use
            }

            val alias = document.createElement("activity-alias")
            alias.setAttribute("android:name", "app.revanced.patches.HiddenAliasLauncher")
            alias.setAttribute("android:targetActivity", mainActivityName)
            alias.setAttribute("android:enabled", "false")
            alias.setAttribute("android:exported", "true")

            val intentFilter = document.createElement("intent-filter")

            val action = document.createElement("action")
            action.setAttribute("android:name", "android.intent.action.MAIN")
            intentFilter.appendChild(action)

            val category = document.createElement("category")
            category.setAttribute("android:name", "android.intent.category.LAUNCHER")
            intentFilter.appendChild(category)

            alias.appendChild(intentFilter)
            application.appendChild(alias)
        }
    }
}



