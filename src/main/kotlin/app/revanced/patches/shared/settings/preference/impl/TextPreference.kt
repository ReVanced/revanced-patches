package app.revanced.patches.shared.settings.preference.impl

import app.revanced.patches.shared.settings.preference.BasePreference
import app.revanced.patches.shared.settings.preference.IResource
import app.revanced.patches.shared.settings.preference.addDefault
import app.revanced.patches.shared.settings.preference.addSummary
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * Text preference.
 *
 * @param key The key of the text preference.
 * @param title The title of the text preference.
 * @param inputType The input type of the text preference.
 * @param default The default value of the text preference.
 * @param summary The summary of the text preference.
 */
internal class TextPreference(
    key: String,
    title: StringResource,
    var inputType: InputType = InputType.STRING,
    val default: String? = null,
    val summary: StringResource? = null
) : BasePreference(key, title) {
    override val tag: String = "EditTextPreference"

    override fun serialize(ownerDocument: Document, resourceCallback: ((IResource) -> Unit)?): Element {
        return super.serialize(ownerDocument, resourceCallback).apply {
            setAttribute("android:inputType", inputType.type)
            addDefault(default)
            addSummary(summary?.also { resourceCallback?.invoke(it) })
        }
    }
}