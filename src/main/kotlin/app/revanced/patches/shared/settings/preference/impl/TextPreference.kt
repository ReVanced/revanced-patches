package app.revanced.patches.shared.settings.preference.impl

import app.revanced.patches.shared.settings.preference.DefaultBasePreference
import org.w3c.dom.Document

/**
 * A text preference.
 *
 * @param key The key of the text preference.
 * @param titleKey The title of the text preference.
 * @param inputType The input type of the text preference.
 * @param summaryKey The summary of the text preference.
 * @param default The default value of the text preference.
 */
class TextPreference(
    key: String?,
    titleKey: String,
    summaryKey: String?,
    val inputType: InputType = InputType.TEXT,
    default: String? = null,
    tag: String = "app.revanced.integrations.settingsmenu.ResettableEditTextPreference"
) : DefaultBasePreference<String>(key, titleKey, summaryKey, tag, default) {

    override fun serialize(ownerDocument: Document) =
        super.serialize(ownerDocument).apply {
            setAttribute("android:inputType", inputType.type)
        }
}