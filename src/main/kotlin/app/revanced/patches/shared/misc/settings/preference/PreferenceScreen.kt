package app.revanced.patches.shared.misc.settings.preference

import app.revanced.patches.shared.misc.settings.preference.PreferenceScreen.SortStyle
import app.revanced.util.resource.BaseResource
import org.w3c.dom.Document

/**
 * A preference screen.
 *
 * @param key The key of the preference. If null, other parameters must be specified.
 * @param titleKey The key of the preference title.
 * @param summaryKey The key of the preference summary.
 * @param sortStyle Sorting style to use. A null value gives [SortStyle.TITLE].
 * @param tag The tag or full class name of the preference.
 * @param preferences The preferences in this screen.
 */
@Suppress("MemberVisibilityCanBePrivate")
open class PreferenceScreen(
    key: String? = null,
    titleKey: String = "${key}_title",
    summaryKey: String? = "${key}_summary",
    sortStyle: SortStyle? = null,
    tag: String = "PreferenceScreen",
    val preferences: Set<BasePreference>
    // Alternatively instead of repurposing the group key for sorting, this could be done adding an extra
    // bundle to the preference xml.  But doing that appears to require bundling and referencing
    // an additional xml file or adding new attributes to the attrs.xml file.
    // Since the group key is unused, for now repurposing the unused key is much simpler.
) : BasePreference(if (sortStyle == null) key else (key + sortStyle.sortSuffix), titleKey, summaryKey, tag) {

    /**
     * Specifies how Integrations should sort a PreferenceScreen or PreferenceGroup.
     */
    enum class SortStyle {
        /**
         * Sort by localized preference title
         */
        TITLE("_sort_title"),
        /**
         * Sort by the preference keys.
         */
        KEY("_sort_key"),
        /**
         * Use original order creating during patching.
         */
        UNSORTED("_sort_ignore");

        constructor(sortSuffix: String) {
            this.sortSuffix = sortSuffix
        }

        val sortSuffix: String
    }

    override fun serialize(ownerDocument: Document, resourceCallback: (BaseResource) -> Unit) =
        super.serialize(ownerDocument, resourceCallback).apply {
            preferences.forEach {
                appendChild(it.serialize(ownerDocument, resourceCallback))
            }
        }
}
