package app.revanced.patches.shared.settings.preference.impl

import app.revanced.patches.shared.settings.preference.BasePreference
import app.revanced.patches.shared.settings.preference.IResource
import app.revanced.patches.shared.settings.preference.addSummary
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * A simple static title and summary that is not backed by any preference key/value,
 * and cannot be changed by or interacted with by the user,
 */
internal class NonInteractivePreference(
    title: StringResource,
    val summary: StringResource,
) : BasePreference("", title) {
    override val tag: String = "Preference"

    override fun serialize(ownerDocument: Document, resourceCallback: ((IResource) -> Unit)?): Element {
        return super.serialize(ownerDocument, resourceCallback).apply {
            addSummary(summary.also { resourceCallback?.invoke(it)
                setAttribute("android:selectable", false.toString())
            })
        }
    }
}
