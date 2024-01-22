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

    val entriesKey: String?
    val entryValuesKey: String?

    constructor(
        key: String? = null,
        titleKey: String,
        summaryKey: String?,
        entries: ArrayResource?,
        entryValues: ArrayResource?,
    ) : super(key, titleKey, summaryKey, "ListPreference") {
        this.entries = entries
        this.entryValues = entryValues

        entriesKey = entries?.name
        entryValuesKey = entryValues?.name
    }

    constructor(
        key: String? = null,
        titleKey: String,
        summaryKey: String?,
        entriesKey: String?,
        entryValuesKey: String?,
    ) : super(key, titleKey, summaryKey, "ListPreference") {
        entries = null
        entryValues = null

        this.entriesKey = entriesKey
        this.entryValuesKey = entryValuesKey
    }

    constructor(
        key: String,
        entriesKey: String,
        entryValuesKey: String,
    ) : super(key, "ListPreference") {
        entries = null
        entryValues = null

        this.entriesKey = entriesKey
        this.entryValuesKey = entryValuesKey
    }

    constructor(
        key: String
    ) : super(key, "ListPreference") {
        entries = null
        entryValues = null

        entriesKey = null
        entryValuesKey = null
    }


    override fun serialize(ownerDocument: Document, resourceCallback: (BaseResource) -> Unit) =
        super.serialize(ownerDocument, resourceCallback).apply {
            val entriesArrayName = entries?.also { resourceCallback.invoke(it) }?.name ?: entriesKey
            val entryValuesArrayName = entryValues?.also { resourceCallback.invoke(it) }?.name ?: entryValuesKey

            entriesArrayName?.let {
                setAttribute(
                    "android:entries",
                    "@array/$it"
                )
            }

            entryValuesArrayName?.let {
                setAttribute(
                    "android:entryValues",
                    "@array/$it"
                )
            }
        }
}