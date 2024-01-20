package app.revanced.patches.shared.misc.settings.preference.impl

import app.revanced.patches.shared.misc.settings.preference.BasePreference
import app.revanced.util.resource.BaseResource
import org.w3c.dom.Document

/**
 * A preference category.
 */
@Suppress("MemberVisibilityCanBePrivate")
open class PreferenceCategory : BasePreference {
    val preferences: Set<BasePreference>

    constructor(
        key: String? = null,
        titleKey: String,
        preferences: Set<BasePreference>,
    ) : super(key, titleKey, null, "PreferenceCategory") {
        this.preferences = preferences
    }

    constructor(
        key: String,
        preferences: Set<BasePreference>,
    ) : super(key, "PreferenceCategory") {
        this.preferences = preferences
    }

    override fun serialize(ownerDocument: Document, resourceCallback: (BaseResource) -> Unit) =
        super.serialize(ownerDocument, resourceCallback).apply {
            preferences.forEach {
                appendChild(it.serialize(ownerDocument, resourceCallback))
            }
        }
}
