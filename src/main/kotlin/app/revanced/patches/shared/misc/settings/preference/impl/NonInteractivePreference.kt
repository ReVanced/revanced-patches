package app.revanced.patches.shared.misc.settings.preference.impl

import app.revanced.patches.shared.misc.settings.preference.BasePreference
import app.revanced.util.resource.BaseResource
import org.w3c.dom.Document

/**
 * A non-interactive preference.
 */
@Suppress("MemberVisibilityCanBePrivate")
class NonInteractivePreference(
    titleKey: String,
    summaryKey: String?,
    tag: String = "Preference",
    val selectable: Boolean = false
) : BasePreference(null, titleKey, summaryKey, tag) {

    override fun serialize(ownerDocument: Document, resourceCallback: (BaseResource) -> Unit) =
        super.serialize(ownerDocument, resourceCallback).apply {
            setAttribute("android:selectable", selectable.toString())
        }
}
