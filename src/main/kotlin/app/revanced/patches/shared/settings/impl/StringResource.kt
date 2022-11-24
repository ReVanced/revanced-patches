package app.revanced.patches.shared.settings.impl

import app.revanced.patches.shared.settings.BaseResource
import app.revanced.patches.shared.settings.IResource
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * Represents a string value in the strings.xml file
 *
 * @param name The name of the string
 * @param value The value of the string
 * @param formatted If the string is formatted. If false, the attribute will be set
 */
internal data class StringResource(
    override val name: String,
    val value: String,
    val formatted: Boolean = true
) : BaseResource(name) {
    override val tag = "string"

    override fun serialize(ownerDocument: Document, resourceCallback: ((IResource) -> Unit)?): Element {
        return super.serialize(ownerDocument, resourceCallback).apply {
            // if the string is un-formatted, explicitly add the formatted attribute
            if (!formatted)
                setAttribute("formatted", "false")

            textContent = value
        }
    }
}
