package app.revanced.patches.shared.misc.settings.preference.impl

import app.revanced.patches.shared.misc.settings.preference.BasePreference
import app.revanced.util.resource.BaseResource
import app.revanced.util.resource.StringResource
import org.w3c.dom.Document

/**
 * A preference that opens an intent.
 *
 * @param key The key of the preference.
 * @param title The title of the preference.
 * @param summary The summary of the text preference.
 * @param intent The intent of the preference.
 */
class IntentPreference(
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
                intentNode.setAttribute("android:data", intent.data)
                intentNode.setAttribute("android:targetClass", intent.targetClass)
                intentNode.setAttribute("android:targetPackage", intent.targetPackageSupplier())
            })
        }

    class Intent(
        internal val data: String,
        internal val targetClass: String,
        internal val targetPackageSupplier: () -> String,
    )
}