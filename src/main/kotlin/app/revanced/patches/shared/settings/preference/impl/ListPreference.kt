package app.revanced.patches.shared.settings.preference.impl

import app.revanced.patches.shared.settings.preference.BasePreference
import app.revanced.patches.shared.settings.preference.addSummary
import org.w3c.dom.Document

/**
 * List preference.
 *
 * @param key The key of the list preference.
 * @param titleKey The title of the list preference.
 * @param entries The human-readable entries of the list preference.
 * @param entryValues The entry values of the list preference.
 * @param summaryKey The summary of the list preference.
 */
class ListPreference(
    key: String,
    titleKey: String,
    summaryKey: String?,
    val entries: ArrayResource?,
    val entryValues: ArrayResource?
) : BasePreference(key, titleKey, summaryKey, "ListPreference") {

    override fun serialize(ownerDocument: Document) =
        super.serialize(ownerDocument).apply {
            if (entries != null) {
                entries.include()
                setAttribute("android:entries", "@array/${entries.name}")
            }
            if (entryValues != null) {
                entryValues.include()
                setAttribute("android:entryValues", "@array/${entryValues.name}")
            }
            addSummary(summaryKey)
        }
}