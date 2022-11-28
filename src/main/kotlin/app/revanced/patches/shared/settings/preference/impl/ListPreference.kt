package app.revanced.patches.shared.settings.preference.impl

import app.revanced.patches.shared.settings.preference.BasePreference
import app.revanced.patches.shared.settings.preference.IResource
import app.revanced.patches.shared.settings.preference.addDefault
import app.revanced.patches.shared.settings.preference.addSummary
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * List preference.
 *
 * @param key The key of the list preference.
 * @param title The title of the list preference.
 * @param entries The human-readable entries of the list preference.
 * @param entryValues The entry values of the list preference.
 * @param default The default entry value of the list preference.
 * @param summary The summary of the list preference.
 */
internal class ListPreference(
    key: String,
    title: StringResource,
    val entries: ArrayResource,
    val entryValues: ArrayResource,
    val default: String? = null,
    val summary: StringResource? = null
) : BasePreference(key, title) {
    override val tag: String = "ListPreference"

    override fun serialize(ownerDocument: Document, resourceCallback: ((IResource) -> Unit)?): Element {
        return super.serialize(ownerDocument, resourceCallback).apply {
            setAttribute("android:entries", "@array/${entries.also { resourceCallback?.invoke(it) }.name}")
            setAttribute("android:entryValues", "@array/${entryValues.also { resourceCallback?.invoke(it) }.name}")
            addDefault(default)
            addSummary(summary)
        }
    }
}