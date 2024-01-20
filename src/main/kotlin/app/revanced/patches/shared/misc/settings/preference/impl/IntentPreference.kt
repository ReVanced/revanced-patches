package app.revanced.patches.shared.misc.settings.preference.impl

import app.revanced.patches.shared.misc.settings.preference.BasePreference
import app.revanced.util.resource.BaseResource
import org.w3c.dom.Document

/**
 * A preference that opens an intent.
 */
class IntentPreference : BasePreference {
    val intent: Intent

    constructor(
        titleKey: String,
        summaryKey: String?,
        intent: Intent
    ) : super(null, titleKey, summaryKey, "Preference") {
        this.intent = intent
    }

    constructor(key: String, intent: Intent) : super(key, "Preference") {
        this.intent = intent
    }

    override fun serialize(ownerDocument: Document, resourceCallback: (BaseResource) -> Unit) =
        super.serialize(ownerDocument, resourceCallback).apply {
            appendChild(ownerDocument.createElement("intent").also { intentNode ->
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
