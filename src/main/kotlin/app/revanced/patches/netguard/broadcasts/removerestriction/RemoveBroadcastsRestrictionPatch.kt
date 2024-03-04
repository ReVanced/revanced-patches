package app.revanced.patches.netguard.broadcasts.removerestriction

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import org.w3c.dom.Element

@Patch(
    name = "Remove broadcasts restriction",
    description = "Enables starting/stopping NetGuard via broadcasts.",
    compatiblePackages = [CompatiblePackage("eu.faircode.netguard")],
    use = false,
)
@Suppress("unused")
object RemoveBroadcastsRestrictionPatch : ResourcePatch() {
    override fun execute(context: ResourceContext) {
        context.xmlEditor["AndroidManifest.xml"].use { editor ->
            val document = editor.file

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
