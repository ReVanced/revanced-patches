package app.revanced.patches.shared.settings.preference.impl

import app.revanced.patches.shared.settings.preference.BasePreference
import org.w3c.dom.Document

/**
 * A preference object.
 *
 * @param key The key of the preference.
 * @param titleKey The title of the preference.
 * @param summaryKey The summary of the text preference.
 * @param intent The intent of the preference.
 */
class Preference(
    key: String,
    titleKey: String,
    summaryKey: String,
    val intent: Intent
) : BasePreference(key, titleKey, summaryKey, "Preference") {

    constructor(
        titleKey: String,
        summaryKey: String,
        intent: Intent
    ) : this("", titleKey, summaryKey, intent)

    override fun serialize(ownerDocument: Document) =
        super.serialize(ownerDocument).apply {
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