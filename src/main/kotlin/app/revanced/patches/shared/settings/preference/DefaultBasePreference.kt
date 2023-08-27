package app.revanced.patches.shared.settings.preference

import app.revanced.patches.shared.settings.preference.impl.StringResource
import org.w3c.dom.Document

/**
 * Base preference class that also has a default value.
 *
 * @param key The key of the preference.
 * @param title The title of the preference.
 * @param tag The tag of the preference.
 * @param summary The summary of the preference.
 * @param default The default value of the preference.
 */
abstract class DefaultBasePreference<T>(
    key: String?,
    title: StringResource,
    summary: StringResource? = null,
    tag: String,
    val default: T? = null,
) : BasePreference(key, title, summary, tag) {

    /**
     * Serialize preference element to XML.
     * Overriding methods should invoke super and operate on its return value.
     * @param ownerDocument Target document to create elements from.
     * @param resourceCallback Called when a resource has been processed.
     * @return The serialized element.
     */
    override fun serialize(ownerDocument: Document, resourceCallback: (BaseResource) -> Unit) =
        super.serialize(ownerDocument, resourceCallback).apply { addDefault(default) }
}