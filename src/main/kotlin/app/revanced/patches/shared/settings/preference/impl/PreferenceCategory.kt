package app.revanced.patches.shared.settings.preference.impl

import app.revanced.patches.shared.settings.preference.BasePreference
import app.revanced.patches.shared.settings.preference.BaseResource
import org.w3c.dom.Document

/**
 * A preference category.
 *
 * @param key The key of the preference.
 * @param title The title of the preference.
 * @param preferences Child preferences of this category.
 */
open class PreferenceCategory(
    key: String,
    title: StringResource,
    var preferences: List<BasePreference>,
    tag: String = "PreferenceCategory"
) : BasePreference(key, title, null, tag) {
    override fun serialize(ownerDocument: Document, resourceCallback: (BaseResource) -> Unit) =
        super.serialize(ownerDocument, resourceCallback).apply {
            for (childPreference in preferences) {
                this.appendChild(childPreference.serialize(ownerDocument, resourceCallback))
            }
        }
}