package app.revanced.util

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.util.DomFileEditor
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.nio.file.Files
import java.nio.file.StandardCopyOption

val classLoader: ClassLoader = object {}.javaClass.classLoader

fun Node.adoptChild(tagName: String, block: Element.() -> Unit) {
    val child = ownerDocument.createElement(tagName)
    child.block()
    appendChild(child)
}

fun Node.cloneNodes(parent: Node) {
    val node = cloneNode(true)
    parent.appendChild(node)
    parent.removeChild(this)
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

fun Node.insertNode(tagName: String, targetNode: Node, block: Element.() -> Unit) {
    val child = ownerDocument.createElement(tagName)
    child.block()
    parentNode.insertBefore(child, targetNode)
}

fun String.startsWithAny(vararg prefixes: String): Boolean {
    for (prefix in prefixes)
        if (this.startsWith(prefix))
            return true

    return false
}

/**
 * Copy resources from the current class loader to the resource directory.
 * @param sourceResourceDirectory The source resource directory name.
 * @param resources The resources to copy.
 */
fun ResourceContext.copyResources(
    sourceResourceDirectory: String,
    vararg resources: ResourceGroup
) {
    val targetResourceDirectory = this["res"]

    for (resourceGroup in resources) {
        resourceGroup.resources.forEach { resource ->
            val resourceFile = "${resourceGroup.resourceDirectoryName}/$resource"
            Files.copy(
                classLoader.getResourceAsStream("$sourceResourceDirectory/$resourceFile")!!,
                targetResourceDirectory.resolve(resourceFile).toPath(),
                StandardCopyOption.REPLACE_EXISTING
            )
        }
    }
}

/**
 * Resource names mapped to their corresponding resource data.
 * @param resourceDirectoryName The name of the directory of the resource.
 * @param resources A list of resource names.
 */
class ResourceGroup(val resourceDirectoryName: String, vararg val resources: String)

/**
 * Copy resources from the current class loader to the resource directory.
 * @param resourceDirectory The directory of the resource.
 * @param targetResource The target resource.
 * @param elementTag The element to copy.
 */
fun ResourceContext.copyXmlNode(
    resourceDirectory: String,
    targetResource: String,
    elementTag: String
) {
    val stringsResourceInputStream =
        classLoader.getResourceAsStream("$resourceDirectory/$targetResource")!!

    // Copy nodes from the resources node to the real resource node
    elementTag.copyXmlNode(
        this.xmlEditor[stringsResourceInputStream],
        this.xmlEditor["res/$targetResource"]
    ).close()
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