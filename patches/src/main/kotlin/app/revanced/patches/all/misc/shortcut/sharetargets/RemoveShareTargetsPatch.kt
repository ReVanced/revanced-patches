package app.revanced.patches.all.misc.shortcut.sharetargets

import app.revanced.patcher.patch.creatingResourcePatch
import app.revanced.util.asSequence
import app.revanced.util.getNode
import org.w3c.dom.Element
import java.io.FileNotFoundException
import java.util.logging.Logger

@Suppress("unused", "ObjectPropertyName")
val `Remove share targets` = creatingResourcePatch(
    description = "Removes share targets like directly sharing to a frequent contact.",
    use = false,
) {
    apply {
        try {
            document("res/xml/shortcuts.xml")
        } catch (_: FileNotFoundException) {
            return@apply Logger.getLogger(this::class.java.name).warning(
                "The app has no shortcuts. No changes applied.",
            )
        }.use { document ->
            val rootNode = document.getNode("shortcuts") as? Element ?: return@use

            document.getElementsByTagName("share-target").asSequence().forEach {
                rootNode.removeChild(it)
            }
        }
    }
}
