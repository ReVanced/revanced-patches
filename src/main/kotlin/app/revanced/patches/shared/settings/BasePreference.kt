package app.revanced.patches.shared.settings

import app.revanced.patches.shared.settings.impl.StringResource
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * Base preference class for all preferences.
 *
 * @param key The key of the preference.
 * @param title The title of the preference.
 */
internal abstract class BasePreference(
    override val key: String,
    override val title: StringResource,
) : IPreference {

    /**
     * Serialize preference element to XML.
     * Overriding methods should invoke super and operate on its return value.
     * @param ownerDocument Target document to create elements from.
     * @param resourceCallback Called when a resource has been processed.
     */
    open fun serialize(ownerDocument: Document, resourceCallback: ((IResource) -> Unit)? = null): Element {
        return ownerDocument.createElement(tag).apply {
            if(key.isNotEmpty())
                setAttribute("android:key", key)
            setAttribute("android:title", "@string/${title.also { resourceCallback?.invoke(it) }.name}")
        }
    }
}