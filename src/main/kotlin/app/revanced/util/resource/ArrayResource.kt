package app.revanced.util.resource

import app.revanced.util.childNodesSequence
import org.w3c.dom.Document
import org.w3c.dom.Node

/**
 *  An array resource.
 *
 *  @param name The name of the array resource.
 *  @param items The items of the array resource.
 *  @param literalValues Whether the items are literal values or references to string resources.
 */
@Suppress("MemberVisibilityCanBePrivate")
class ArrayResource(
    name: String,
    val items: List<String>,
    val literalValues: Boolean = false
) : BaseResource(name, "string-array") {
    override fun serialize(ownerDocument: Document, resourceCallback: (BaseResource) -> Unit) =
        super.serialize(ownerDocument, resourceCallback).apply {
            setAttribute("name", name)

            items.forEach { item ->
                appendChild(ownerDocument.createElement("item").also { itemNode ->
                    itemNode.textContent = if (literalValues) item else "@string/$item"
                })
            }
        }

    companion object {
        fun fromNode(node: Node): ArrayResource {
            val key = node.attributes.getNamedItem("name").textContent

            val items = node.childNodesSequence().map { item -> item.textContent }.toList()
            // TODO: This is a bit of a hack, as some items could be literal values and some could be references.
            val literalValues = items.any { item -> !item.startsWith("@string/") }

            return ArrayResource(key, items, literalValues)
        }
    }
}