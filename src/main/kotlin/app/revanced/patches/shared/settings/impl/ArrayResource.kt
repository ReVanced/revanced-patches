package app.revanced.patches.shared.settings.impl

import app.revanced.patches.shared.settings.BaseResource
import app.revanced.patches.shared.settings.IResource
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 *  Represents an array resource.
 *
 *  @param name The name of the array resource.
 *  @param items The items of the array resource.
 */
internal data class ArrayResource(
    override val name: String,
    val items: List<StringResource>
) : BaseResource(name) {
    override val tag = "string-array"

    override fun serialize(ownerDocument: Document, resourceCallback: ((IResource) -> Unit)?): Element {
        return super.serialize(ownerDocument, resourceCallback).apply {
            items.forEach { item ->
                setAttribute("name", item.also { resourceCallback?.invoke(it) }.name)

                this.appendChild(ownerDocument.createElement("item").also { itemNode ->
                    itemNode.textContent = item.value
                })
            }
        }
    }
}