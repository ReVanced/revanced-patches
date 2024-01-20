package app.revanced.patches.shared.misc.settings.preference.impl

import app.revanced.patches.shared.misc.settings.preference.BasePreference
import app.revanced.util.resource.BaseResource
import org.w3c.dom.Document

/**
 * A preference screen.
 */
@Suppress("MemberVisibilityCanBePrivate")
open class PreferenceScreen : BasePreference {
    val preferences: Set<BasePreference>

    constructor(
        key: String? = null,
        titleKey: String,
        summaryKey: String? = null,
        preferences: Set<BasePreference>,
    ) : super(key, titleKey, summaryKey, "PreferenceScreen") {
        this.preferences = preferences
    }

    constructor(
        key: String,
        preferences: Set<BasePreference>,
    ) : super(key, "PreferenceScreen") {
        this.preferences = preferences
    }

    override fun serialize(ownerDocument: Document, resourceCallback: (BaseResource) -> Unit) =
        super.serialize(ownerDocument, resourceCallback).apply {
            preferences.forEach {
                this.appendChild(it.serialize(ownerDocument, resourceCallback))
            }
        }
}
