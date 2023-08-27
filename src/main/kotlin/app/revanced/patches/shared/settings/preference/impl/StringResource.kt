package app.revanced.patches.shared.settings.preference.impl

import app.revanced.patches.shared.settings.preference.BaseResource
import org.w3c.dom.Document

/**
 * A string value.
 * Represets a string in the strings.xml file.
 *
 * @param name The name of the string.
 * @param value The value of the string.
 * @param formatted If the string is formatted. If false, the attribute will be set.
 */
class StringResource(
    name: String,
    val value: String,
    val formatted: Boolean = true
) : BaseResource(name, "string") {

    override fun serialize(ownerDocument: Document, resourceCallback: (BaseResource) -> Unit) =
        super.serialize(ownerDocument, resourceCallback).apply {
            // if the string is un-formatted, explicitly add the formatted attribute
            if (!formatted) setAttribute("formatted", "false")

            textContent = value
        }
}
