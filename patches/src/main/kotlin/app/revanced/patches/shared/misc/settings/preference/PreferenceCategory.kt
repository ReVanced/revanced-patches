package app.revanced.patches.shared.misc.settings.preference

import app.revanced.util.resource.BaseResource
import org.w3c.dom.Document

/**
 * A preference category.
 *
 * @param key The key of the preference. If null, other parameters must be specified.
 * @param titleKey The key of the preference title.
 * @param tag The tag or full class name of the preference.
 * @param preferences The preferences in this category.
 */
@Suppress("MemberVisibilityCanBePrivate")
open class PreferenceCategory(
    key: String? = null,
    titleKey: String = "${key}_title",
    tag: String = "PreferenceCategory",
    val preferences: Set<BasePreference>
) : BasePreference(key, titleKey, null, tag) {

    override fun serialize(ownerDocument: Document, resourceCallback: (BaseResource) -> Unit) =
        super.serialize(ownerDocument, resourceCallback).apply {
            preferences.forEach {
                appendChild(it.serialize(ownerDocument, resourceCallback))
            }
        }
}
