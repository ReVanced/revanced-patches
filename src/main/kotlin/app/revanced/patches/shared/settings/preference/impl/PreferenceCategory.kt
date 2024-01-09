package app.revanced.patches.shared.settings.preference.impl

import app.revanced.patches.shared.settings.preference.BasePreference
import org.w3c.dom.Document

/**
 * A preference category.
 *
 * @param key The key of the preference.
 * @param titleKey The title of the preference.
 * @param preferences Child preferences of this category.
 */
open class PreferenceCategory(
    key: String,
    titleKey: String,
    var preferences: List<BasePreference>,
    tag: String = "PreferenceCategory"
) : BasePreference(key, titleKey, null, tag) {

    override fun serialize(ownerDocument: Document) =
        super.serialize(ownerDocument).apply {
            for (childPreference in preferences) {
                this.appendChild(childPreference.serialize(ownerDocument))
            }
        }
}