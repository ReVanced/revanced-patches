package app.revanced.patches.shared.misc.settings.preference.impl

import app.revanced.patches.shared.misc.settings.preference.BasePreference
import app.revanced.util.resource.ArrayResource
import app.revanced.util.resource.BaseResource
import org.w3c.dom.Document

/**
 * List preference.
 */
@Suppress("MemberVisibilityCanBePrivate")
class ListPreference : BasePreference {
    val entries: ArrayResource?
    val entryValues: ArrayResource?

    constructor(
        key: String? = null,
        titleKey: String,
        summaryKey: String?,
        entries: ArrayResource?,
        entryValues: ArrayResource?,
    ) : super(key, titleKey, summaryKey, "ListPreference") {
        this.entries = entries
        this.entryValues = entryValues
    }

    constructor(
        key: String
    ) : super(key, "ListPreference") {
        entries = null
        entryValues = null
    }


    override fun serialize(ownerDocument: Document, resourceCallback: (BaseResource) -> Unit) =
        super.serialize(ownerDocument, resourceCallback).apply {
            entries?.let {
                setAttribute(
                    "android:entries",
                    "@array/${entries.also { resourceCallback.invoke(it) }.name}"
                )
            }
            entryValues?.let {
                setAttribute(
                    "android:entryValues",
                    "@array/${entryValues.also { resourceCallback.invoke(it) }.name}"
                )
            }
        }
}