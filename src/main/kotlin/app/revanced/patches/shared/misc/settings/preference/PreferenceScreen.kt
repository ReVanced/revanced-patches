package app.revanced.patches.shared.misc.settings.preference

import app.revanced.util.resource.BaseResource
import org.w3c.dom.Document

/**
 * A preference screen.
 *
 * @param key The key of the preference. If null, other parameters must be specified.
 * @param titleKey The key of the preference title.
 * @param summaryKey The key of the preference summary.
 * @param sorting Sorting to use. If the sorting is not [Sorting.UNSORTED],
 *                then the key parameter will be modified to include the sort type.
 * @param tag The tag or full class name of the preference.
 * @param preferences The preferences in this screen.
 */
@Suppress("MemberVisibilityCanBePrivate")
open class PreferenceScreen(
    key: String? = null,
    titleKey: String = "${key}_title",
    summaryKey: String? = "${key}_summary",
    sorting: Sorting = Sorting.BY_TITLE,
    tag: String = "PreferenceScreen",
    val preferences: Set<BasePreference>,
    // Alternatively, instead of repurposing the key for sorting,
    // an extra bundle parameter can be added to the preferences XML declaration.
    // This would require bundling and referencing an additional XML file
    // or adding new attributes to the attrs.xml file.
    // Since the key value is not currently used by integrations,
    // for now it's much simpler to modify the key to include the sort parameter.
) : BasePreference(if (sorting == Sorting.UNSORTED) key else (key + sorting.keySuffix), titleKey, summaryKey, tag) {
    override fun serialize(ownerDocument: Document, resourceCallback: (BaseResource) -> Unit) =
        super.serialize(ownerDocument, resourceCallback).apply {
            preferences.forEach {
                appendChild(it.serialize(ownerDocument, resourceCallback))
            }
        }

    /**
     * How a PreferenceScreen should be sorted.
     */
    enum class Sorting(val keySuffix: String) {
        /**
         * Sort by the localized preference title.
         */
        BY_TITLE("_sort_by_title"),

        /**
         * Sort by the preference keys.
         */
        BY_KEY("_sort_by_key"),

        /**
         * Unspecified sorting.
         */
        UNSORTED("_sort_by_unsorted"),
    }
}
