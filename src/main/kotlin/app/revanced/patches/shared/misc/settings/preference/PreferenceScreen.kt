package app.revanced.patches.shared.misc.settings.preference

import app.revanced.patches.shared.misc.settings.preference.PreferenceScreen.Sorting
import app.revanced.util.resource.BaseResource
import org.w3c.dom.Document

/**
 * A preference screen.
 *
 * @param key The key of the preference. If null, other parameters must be specified.
 * @param titleKey The key of the preference title.
 * @param summaryKey The key of the preference summary.
 * @param sorting Sorting to use. If null, the preference will be sorted according to [Sorting.BY_TITLE].
 *                Note: Using a non null sorting will modify the key of this screen.
 * @param tag The tag or full class name of the preference.
 * @param preferences The preferences in this screen.
 */
@Suppress("MemberVisibilityCanBePrivate")
open class PreferenceScreen(
    key: String? = null,
    titleKey: String = "${key}_title",
    summaryKey: String? = "${key}_summary",
    sorting: Sorting? = null,
    tag: String = "PreferenceScreen",
    val preferences: Set<BasePreference>,
    // Alternatively, instead of repurposing the key for sorting, an extra
    // bundle can be added to the preferences XML file. This would require bundling and referencing
    // an additional XML file or adding new attributes to the attrs.xml file.
    // Since the key is unused, for now repurposing the unused key is much simpler.
) : BasePreference(if (sorting == null) key else (key + sorting.keySuffix), titleKey, summaryKey, tag) {
    override fun serialize(ownerDocument: Document, resourceCallback: (BaseResource) -> Unit) =
        super.serialize(ownerDocument, resourceCallback).apply {
            preferences.forEach {
                appendChild(it.serialize(ownerDocument, resourceCallback))
            }
        }

    /**
     * How a PreferenceScreen or PreferenceGroup should be sorted.
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
