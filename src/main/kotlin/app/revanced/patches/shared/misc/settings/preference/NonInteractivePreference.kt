package app.revanced.patches.shared.misc.settings.preference

import app.revanced.util.resource.BaseResource
import org.w3c.dom.Document

/**
 * A non-interactive preference.
 *
 * Typically used to present static text, but also used for custom integration code that responds to taps.
 *
 * @param key The preference key.
 * @param summaryKey The preference summary key.
 * @param tag The tag or full class name of the preference.
 * @param selectable If the preference is selectable and responds to tap events.
 */
@Suppress("MemberVisibilityCanBePrivate")
class NonInteractivePreference(
    key: String,
    titleKey: String = "${key}_title",
    summaryKey: String? = "${key}_summary",
    tag: String = "Preference",
    val selectable: Boolean = false
) : BasePreference(key, titleKey, summaryKey, tag) {

    @Deprecated("Here only for binary compatibility, and should be removed after the next major version update.")
    constructor(
        key: String,
        summaryKey: String? = "${key}_summary",
        tag: String = "Preference",
        selectable: Boolean = false
    ) : this(key, "${key}_title", summaryKey, tag, selectable)

    override fun serialize(ownerDocument: Document, resourceCallback: (BaseResource) -> Unit) =
        super.serialize(ownerDocument, resourceCallback).apply {
            setAttribute("android:selectable", selectable.toString())
        }
}
