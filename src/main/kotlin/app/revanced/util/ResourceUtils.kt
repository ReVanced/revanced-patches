package app.revanced.util

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.util.DomFileEditor
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.util.resource.BaseResource
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption

private val classLoader = object {}.javaClass.classLoader

/**
 * Returns a sequence for all child nodes.
 */
fun NodeList.asSequence() = (0 until this.length).asSequence().map { this.item(it) }

/**
 * Returns a sequence for all child nodes.
 */
fun Node.childElementsSequence() = this.childNodes.asSequence().filter { it.nodeType == Node.ELEMENT_NODE }

/**
 * Performs the given [action] on each child element.
 */
fun Node.forEachChildElement(action: (Node) -> Unit) = childElementsSequence().forEach {
    action(it)
}

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

/**
 * Add a resource node child.
 *
 * @param resource The resource to add.
 * @param resourceCallback Called when a resource has been processed.
 */
internal fun Node.addResource(resource: BaseResource, resourceCallback: (BaseResource) -> Unit = { }) {
    appendChild(resource.serialize(ownerDocument, resourceCallback))
}

internal fun DomFileEditor?.getNode(tagName: String) = this!!.file.getElementsByTagName(tagName).item(0)