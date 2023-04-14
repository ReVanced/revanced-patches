package app.revanced.patches.shared.settings.preference.impl

import app.revanced.patches.shared.settings.preference.BaseResource
import app.revanced.patches.shared.settings.preference.IResource
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 *  Represents an array resource.
 *
 *  @param name The name of the array resource.
 *  @param items The items of the array resource.
 */
// TODO: allow specifying an array resource file instead of using a list of StringResources
internal data class ArrayResource(
    override val name: String,
    val items: List<StringResource>
) : BaseResource(name) {
    override val tag = "string-array"

    override fun serialize(ownerDocument: Document, resourceCallback: ((IResource) -> Unit)?): Element {
        return super.serialize(ownerDocument, resourceCallback).apply {
            setAttribute("name", name)

            items.forEach { item ->
                resourceCallback?.invoke(item)

                this.appendChild(ownerDocument.createElement("item").also { itemNode ->
                    itemNode.textContent = "@string/${item.name}"
                })
            }
        }
    }
}