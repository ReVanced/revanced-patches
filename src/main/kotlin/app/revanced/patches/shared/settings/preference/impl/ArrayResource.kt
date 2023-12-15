package app.revanced.patches.shared.settings.preference.impl

import app.revanced.patches.shared.settings.AbstractSettingsResourcePatch
import app.revanced.patches.shared.settings.preference.BaseResource
import org.w3c.dom.Document

/**
 *  An array resource.
 *
 *  @param name The name of the array resource.
 *  @param items The items of the array resource.
 *  @param literalValues If the values are to be used exactly as is.
 *                       If false, the values are treated as string resource names.
 */
class ArrayResource(
    name: String,
    val items: Iterable<String>,
    val literalValues : Boolean = false
) : BaseResource(name, "string-array") {

    override fun serialize(ownerDocument: Document) =
        super.serialize(ownerDocument).apply {
            setAttribute("name", name)

            items.forEach { item ->
                this.appendChild(ownerDocument.createElement("item").also { itemNode ->
                    itemNode.textContent = if (literalValues) item else "@string/$item"
                })
            }
        }

    /**
     * Bundles this array with the target app.
     */
    fun include() = AbstractSettingsResourcePatch.addArray(this)

}