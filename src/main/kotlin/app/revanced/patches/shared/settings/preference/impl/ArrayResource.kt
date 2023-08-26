package app.revanced.patches.shared.settings.preference.impl

import app.revanced.patches.shared.settings.preference.BaseResource
import org.w3c.dom.Document

// TODO: allow specifying an array resource file instead of using a list of StringResources
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
}