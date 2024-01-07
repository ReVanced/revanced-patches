package app.revanced.util.resource

import org.w3c.dom.Document
import java.util.*

/**
 * A string value.
 * Represets a string in the strings.xml file.
 *
 * @param name The name of the string.
 * @param value The value of the string.
 * @param formatted If the string is formatted. Defaults to `true`.
 * @param language The language of the string. Defaults to [Locale.ENGLISH].
 */
class StringResource(
    name: String,
    val value: String,
    val formatted: Boolean = true,
    val language: String = Locale.ENGLISH.language
) : BaseResource(name, "string") {
    override fun serialize(ownerDocument: Document, resourceCallback: (BaseResource) -> Unit) =
        super.serialize(ownerDocument, resourceCallback).apply {
            // if the string is un-formatted, explicitly add the formatted attribute
            if (!formatted) setAttribute("formatted", "false")

            textContent = value
        }
}
