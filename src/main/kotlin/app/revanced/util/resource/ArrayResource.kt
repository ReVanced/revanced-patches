package app.revanced.util.resource

import app.revanced.util.childNodesSequence
import org.w3c.dom.Document
import org.w3c.dom.Node

/**
 *  An array resource.
 *
 *  @param name The name of the array resource.
 *  @param items The items of the array resource.
 */
class ArrayResource(
    name: String,
    val items: List<StringResource>
) : BaseResource(name, "string-array") {
    override fun serialize(ownerDocument: Document, resourceCallback: (BaseResource) -> Unit) =
        super.serialize(ownerDocument, resourceCallback).apply {
            setAttribute("name", name)

            items.forEach { item ->
                resourceCallback.invoke(item)

                this.appendChild(ownerDocument.createElement("item").also { itemNode ->
                    itemNode.textContent = "@string/${item.name}"
                })
            }
        }

    companion object {
        fun fromNode(node: Node): ArrayResource {
            val key = node.attributes.getNamedItem("name").textContent

            val items = node.childNodesSequence().map(StringResource::fromNode).toList()

            return ArrayResource(key, items)
        }
    }
}