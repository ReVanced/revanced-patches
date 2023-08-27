package app.revanced.patches.shared.settings.preference.impl

import app.revanced.patches.shared.settings.preference.BasePreference
import app.revanced.patches.shared.settings.preference.BaseResource
import org.w3c.dom.Document

/**
 * A preference object.
 *
 * @param key The key of the preference.
 * @param title The title of the preference.
 * @param summary The summary of the text preference.
 * @param intent The intent of the preference.
 */
class Preference(
    key: String,
    title: StringResource,
    summary: StringResource,
    val intent: Intent
) : BasePreference(key, title, summary, "Preference") {
    constructor(
        title: StringResource,
        summary: StringResource,
        intent: Intent
    ) : this("", title, summary, intent)

    override fun serialize(ownerDocument: Document, resourceCallback: (BaseResource) -> Unit) =
        super.serialize(ownerDocument, resourceCallback).apply {
            this.appendChild(ownerDocument.createElement("intent").also { intentNode ->
                intentNode.setAttribute("android:targetPackage", intent.targetPackage)
                intentNode.setAttribute("android:data", intent.data)
                intentNode.setAttribute("android:targetClass", intent.targetClass)
            })
        }

    class Intent(
        internal val targetPackage: String,
        internal val data: String,
        internal val targetClass: String
    )
}