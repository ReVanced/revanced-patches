package app.revanced.patches.shared.settings.impl

import app.revanced.patches.shared.settings.BasePreference
import app.revanced.patches.shared.settings.IResource
import app.revanced.patches.shared.settings.addSummary
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * A Preference object.
 *
 * @param title The title of the preference.
 * @param intent The intent of the preference.
 * @param summary The summary of the text preference.
 */
internal class Preference(
    key: String,
    title: StringResource,
    val intent: Intent,
    val summary: StringResource? = null
) : BasePreference(key, title) {
    override val tag: String = "Preference"

    /* Key-less constructor */
    constructor(
        title: StringResource,
        intent: Intent,
        summary: StringResource? = null
    ) : this("", title, intent, summary)

    override fun serialize(ownerDocument: Document, resourceCallback: ((IResource) -> Unit)?): Element {
        return super.serialize(ownerDocument, resourceCallback).apply {
            addSummary(summary?.also { resourceCallback?.invoke(it) })

            this.appendChild(ownerDocument.createElement("intent").also { intentNode ->
                intentNode.setAttribute("android:targetPackage", intent.targetPackage)
                intentNode.setAttribute("android:data", intent.data)
                intentNode.setAttribute("android:targetClass", intent.targetClass)
            })
        }
    }

    data class Intent(val targetPackage: String, val data: String, val targetClass: String)
}