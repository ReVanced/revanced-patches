package app.revanced.patches.shared.settings.preference.impl

import app.revanced.patches.shared.settings.preference.BasePreference
import app.revanced.patches.shared.settings.preference.addSummary
import org.w3c.dom.Document

/**
 * A preference screen.
 *
 * @param key The key of the preference.
 * @param titleKey The title of the preference.
 * @param preferences Child preferences of this screen.
 * @param summaryKey The summary of the text preference.
 */
open class PreferenceScreen(
    key: String,
    titleKey: String,
    var preferences: List<BasePreference>,
    summaryKey: String? = null
) : BasePreference(key, titleKey, summaryKey, "PreferenceScreen") {

    override fun serialize(ownerDocument: Document) =
        super.serialize(ownerDocument).apply {
            addSummary(summaryKey)

            for (childPreference in preferences)
                this.appendChild(childPreference.serialize(ownerDocument))
        }
}