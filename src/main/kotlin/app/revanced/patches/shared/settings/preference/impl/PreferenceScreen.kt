package app.revanced.patches.shared.settings.preference.impl

import app.revanced.patches.shared.settings.preference.BasePreference
import app.revanced.patches.shared.settings.preference.BaseResource
import app.revanced.patches.shared.settings.preference.addSummary
import org.w3c.dom.Document

/**
 * A preference screen.
 *
 * @param key The key of the preference.
 * @param title The title of the preference.
 * @param preferences Child preferences of this screen.
 * @param summary The summary of the text preference.
 */
open class PreferenceScreen(
    key: String,
    title: StringResource,
    var preferences: List<BasePreference>,
    summary: StringResource? = null
) : BasePreference(key, title, summary, "PreferenceScreen") {
    override fun serialize(ownerDocument: Document, resourceCallback: (BaseResource) -> Unit) =
        super.serialize(ownerDocument, resourceCallback).apply {
            addSummary(summary?.also { resourceCallback.invoke(it) })

            for (childPreference in preferences)
                this.appendChild(childPreference.serialize(ownerDocument, resourceCallback))
        }
}