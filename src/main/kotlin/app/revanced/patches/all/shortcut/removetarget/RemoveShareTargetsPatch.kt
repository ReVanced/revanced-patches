package app.revanced.patches.all.shortcut.removetarget

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.util.asSequence
import org.w3c.dom.Element

@Patch(
    name = "Remove all share targets",
    description = "Removes share targets like directly sharing to a frequent contact of an app",
    use = false,
)
@Suppress("unused")
object RemoveShareTargetsPatch : ResourcePatch() {
    override fun execute(context: ResourceContext) {
        val shortcutsXml = context["res/xml/shortcuts.xml", false]
        if (!shortcutsXml.exists()) return

        context.document[shortcutsXml.path].use { document ->
            val rootNode = document.getElementsByTagName("shortcuts").item(0) as? Element ?: return@use

            document.getElementsByTagName("share-target").asSequence().forEach {
                rootNode.removeChild(it)
            }
        }
    }
}
