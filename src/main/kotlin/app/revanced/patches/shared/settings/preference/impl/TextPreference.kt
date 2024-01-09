package app.revanced.patches.shared.settings.preference.impl

import app.revanced.patches.shared.settings.preference.BasePreference
import app.revanced.patches.shared.settings.preference.BaseResource
import org.w3c.dom.Document

/**
 * A text preference.
 *
 * @param key The key of the text preference.
 * @param title The title of the text preference.
 * @param inputType The input type of the text preference.
 * @param summary The summary of the text preference.
 */
class TextPreference(
    key: String?,
    title: StringResource,
    summary: StringResource?,
    val inputType: InputType = InputType.TEXT,
    tag: String = "app.revanced.integrations.shared.settings.preference.ResettableEditTextPreference"
) : BasePreference(key, title, summary, tag) {

    override fun serialize(ownerDocument: Document, resourceCallback: (BaseResource) -> Unit) =
        super.serialize(ownerDocument, resourceCallback).apply {
            setAttribute("android:inputType", inputType.type)
        }
}