package app.revanced.util

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.util.DomFileEditor
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import org.w3c.dom.Node
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption

private val classLoader = object {}.javaClass.classLoader

/**
 * Recursively traverse the DOM tree starting from the given root node.
 *
 * @param action function that is called for every node in the tree.
 */
fun Node.doRecursively(action: (Node) -> Unit) {
    action(this)
    for (i in 0 until this.childNodes.length) this.childNodes.item(i).doRecursively(action)
}

/**
 * Merge strings. This manages [StringResource]s automatically.
 *
 * @param host The hosting xml resource. Needs to be a valid strings.xml resource.
 */
fun ResourceContext.mergeStrings(host: String) {
    this.iterateXmlNodeChildren(host, "resources") {
        // TODO: figure out why this is needed
        if (!it.hasAttributes()) return@iterateXmlNodeChildren

        val attributes = it.attributes
        val key = attributes.getNamedItem("name")!!.nodeValue!!
        val value = it.textContent!!

        val formatted = attributes.getNamedItem("formatted") == null

        SettingsPatch.addString(key, value, formatted)
    }
}

/**
 * Copy resources from the current class loader to the resource directory.
 *
 * @param sourceResourceDirectory The source resource directory name.
 * @param resources The resources to copy.
 */
fun ResourceContext.copyResources(sourceResourceDirectory: String, vararg resources: ResourceGroup) {
    val targetResourceDirectory = this["res"]

    for (resourceGroup in resources) {
        resourceGroup.resources.forEach { resource ->
            val resourceFile = "${resourceGroup.resourceDirectoryName}/$resource"
            Files.copy(
                inputStreamFromBundledResource(sourceResourceDirectory, resourceFile)!!,
                targetResourceDirectory.resolve(resourceFile).toPath(), StandardCopyOption.REPLACE_EXISTING
            )
        }
    }
}

internal fun inputStreamFromBundledResource(
    sourceResourceDirectory: String,
    resourceFile: String
): InputStream? = classLoader.getResourceAsStream("$sourceResourceDirectory/$resourceFile")

/**
 * Resource names mapped to their corresponding resource data.
 * @param resourceDirectoryName The name of the directory of the resource.
 * @param resources A list of resource names.
 */
class ResourceGroup(val resourceDirectoryName: String, vararg val resources: String)

/**
 * Iterate through the children of a node by its tag.
 * @param resource The xml resource.
 * @param targetTag The target xml node.
 * @param callback The callback to call when iterating over the nodes.
 */
fun ResourceContext.iterateXmlNodeChildren(
    resource: String,
    targetTag: String,
    callback: (node: Node) -> Unit
) =
    xmlEditor[classLoader.getResourceAsStream(resource)!!].use {
        val stringsNode = it.file.getElementsByTagName(targetTag).item(0).childNodes
        for (i in 1 until stringsNode.length - 1) callback(stringsNode.item(i))
    }


/**
 * Copies the specified node of the source [DomFileEditor] to the target [DomFileEditor].
 * @param source the source [DomFileEditor].
 * @param target the target [DomFileEditor]-
 * @return AutoCloseable that closes the target [DomFileEditor]s.
 */
fun String.copyXmlNode(source: DomFileEditor, target: DomFileEditor): AutoCloseable {
    val hostNodes = source.file.getElementsByTagName(this).item(0).childNodes

    val destinationResourceFile = target.file
    val destinationNode = destinationResourceFile.getElementsByTagName(this).item(0)

    for (index in 0 until hostNodes.length) {
        val node = hostNodes.item(index).cloneNode(true)
        destinationResourceFile.adoptNode(node)
        destinationNode.appendChild(node)
    }

    return AutoCloseable {
        source.close()
        target.close()
    }
}