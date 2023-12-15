package app.revanced.patches.shared.settings.preference

import org.w3c.dom.Document

/**
 * Base preference class that also has a default value.
 *
 * @param key The key of the preference.
 * @param titleKey The title of the preference.
 * @param tag The tag of the preference.
 * @param summaryKey The summary of the preference.
 * @param default The default value of the preference.
 */
abstract class DefaultBasePreference<T>(
    key: String?,
    titleKey: String,
    summaryKey: String? = null,
    tag: String,
    val default: T? = null,
) : BasePreference(key, titleKey, summaryKey, tag) {

    /**
     * Serialize preference element to XML.
     * Overriding methods should invoke super and operate on its return value.
     * @param ownerDocument Target document to create elements from.
     * @return The serialized element.
     */
    override fun serialize(ownerDocument: Document) =
        super.serialize(ownerDocument).apply { addDefault(default) }
}