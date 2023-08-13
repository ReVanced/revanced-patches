package app.revanced.patches.shared.settings.preference.impl

import app.revanced.patches.shared.settings.preference.BasePreference
import app.revanced.patches.shared.settings.preference.BaseResource
import app.revanced.patches.shared.settings.preference.addSummary
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * A non interactive preference.
 *
 * Not backed by any preference key/value,
 * and cannot be changed by or interacted with by the user.
 *
 * @param title The title of the preference.
 * @param summary The summary of the text preference.
 */
class NonInteractivePreference(
    title: StringResource,
    summary: StringResource,
) : BasePreference(null, title, summary, "Preference") {
    override fun serialize(ownerDocument: Document, resourceCallback: (BaseResource) -> Unit): Element {
        return super.serialize(ownerDocument, resourceCallback).apply {
            addSummary(summary?.also { resourceCallback.invoke(it)
                setAttribute("android:selectable", false.toString())
            })
        }
    }
}
