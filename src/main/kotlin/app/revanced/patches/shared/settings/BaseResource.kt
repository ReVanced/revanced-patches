package app.revanced.patches.shared.settings

import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * Base resource class for all resources.
 *
 * @param name The name of the resource.
 */
internal abstract class BaseResource(
    override val name: String
) : IResource {

    /**
     * Serialize resource element to XML.
     * Overriding methods should invoke super and operate on its return value.
     * @param ownerDocument Target document to create elements from.
     * @param resourceCallback Called when a resource has been processed.
     */
    open fun serialize(ownerDocument: Document, resourceCallback: ((IResource) -> Unit)? = null): Element {
        return ownerDocument.createElement(tag).apply {
            setAttribute("name", name)
        }
    }
}