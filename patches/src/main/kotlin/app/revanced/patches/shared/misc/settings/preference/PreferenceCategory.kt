package app.revanced.patches.shared.misc.settings.preference

import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference.Sorting
import app.revanced.util.resource.BaseResource
import org.w3c.dom.Document

/**
 * A preference category.
 *
 * @param key The key of the preference. If null, other parameters must be specified.
 * @param titleKey The key of the preference title.
 * @param icon The preference icon resource name.
 * @param layout Layout declaration.
 * @param tag The tag or full class name of the preference.
 * @param preferences The preferences in this category.
 */
@Suppress("MemberVisibilityCanBePrivate")
open class PreferenceCategory protected constructor(
    key: String? = null,
    titleKey: String = "${key}_title",
    summaryKey: String? = "${key}_summary",
    icon: String? = null,
    layout: String? = null,
    sorting: Sorting = Sorting.BY_TITLE,
    tag: String = "PreferenceCategory",
    val preferences: Set<BasePreference>
) : BasePreference(
    // Alternatively, instead of repurposing the key for sorting,
    // an extra bundle parameter can be added to the preferences XML declaration.
    // This would require bundling and referencing an additional XML file
    // or adding new attributes to the attrs.xml file.
    // Since the key value is not currently used by the extensions,
    // for now it's much simpler to modify the key to include the sort parameter.
    if (sorting == Sorting.UNSORTED) key else (key + sorting.keySuffix),
    titleKey,
    summaryKey,
    icon,
    layout,
    tag
) {

    constructor(
        key: String? = null,
        titleKey: String = "${key}_title",
        icon: String? = null,
        layout: String? = null,
        sorting: Sorting = Sorting.BY_TITLE,
        tag: String = "PreferenceCategory",
        preferences: Set<BasePreference>
    ) : this(key, titleKey, null, icon, layout, sorting, tag, preferences)

    override fun serialize(ownerDocument: Document, resourceCallback: (BaseResource) -> Unit) =
        super.serialize(ownerDocument, resourceCallback).apply {
            preferences.forEach {
                appendChild(it.serialize(ownerDocument, resourceCallback))
            }
        }
}
