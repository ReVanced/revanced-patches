package app.revanced.patches.all.shortcut.sharetargets

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.util.asSequence
import app.revanced.util.getNode
import org.w3c.dom.Element
import java.io.FileNotFoundException
import java.util.logging.Logger

@Patch(
    name = "Remove share targets",
    description = "Removes share targets like directly sharing to a frequent contact.",
    use = false,
)
@Suppress("unused")
object RemoveShareTargetsPatch : ResourcePatch() {
    override fun execute(context: ResourceContext) {
        try {
            context.document["res/xml/shortcuts.xml"]
        } catch (_: FileNotFoundException) {
            return Logger.getLogger(this::class.java.name).warning("The app has no shortcuts")
        }.use { document ->
            val rootNode = document.getNode("shortcuts") as? Element ?: return@use

            document.getElementsByTagName("share-target").asSequence().forEach {
                rootNode.removeChild(it)
            }
        }
    }
}
