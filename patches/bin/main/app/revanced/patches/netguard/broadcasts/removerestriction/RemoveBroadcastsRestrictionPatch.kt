package app.revanced.patches.netguard.broadcasts.removerestriction

import app.revanced.patcher.patch.resourcePatch
import org.w3c.dom.Element

@Suppress("unused")
val removeBroadcastsRestrictionPatch = resourcePatch(
    name = "Remove broadcasts restriction",
    description = "Enables starting/stopping NetGuard via broadcasts.",
) {
    compatibleWith("eu.faircode.netguard")

    execute {
        document("AndroidManifest.xml").use { document ->
            val applicationNode =
                document
                    .getElementsByTagName("application")
                    .item(0) as Element

            applicationNode.getElementsByTagName("receiver").also { list ->
                for (i in 0 until list.length) {
                    val element = list.item(i) as? Element ?: continue
                    if (element.getAttribute("android:name") == "eu.faircode.netguard.WidgetAdmin") {
                        element.removeAttribute("android:permission")
                        break
                    }
                }
            }
        }
    }
}
