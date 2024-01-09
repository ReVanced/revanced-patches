package app.revanced.patches.shared.settings.preference

import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * Base preference class for all preferences.
 *
 * @param key The key of the preference.
 * @param titleKey The title of the preference.
 * @param tag The full class name for the preference.
 * @param summaryKey The summary of the preference.
 */
abstract class BasePreference(
    val key: String?,
    val titleKey: String,
    val summaryKey: String? = null,
    val tag: String
) {
    /**
     * Serialize preference element to XML.
     * Overriding methods should invoke super and operate on its return value.
     * @param ownerDocument Target document to create elements from.
     * @return The serialized element.
     */
    open fun serialize(ownerDocument: Document): Element =
        ownerDocument.createElement(tag).apply {
            if (key != null) setAttribute("android:key", key)
            setAttribute("android:title", "@string/$titleKey")
            addSummary(summaryKey)
        }
}