package app.revanced.patches.shared.misc.settings.preference

import app.revanced.util.resource.BaseResource
import org.w3c.dom.Document

/**
 * A preference screen.
 *
 * @param key The key of the preference. If null, other parameters must be specified.
 * @param titleKey The key of the preference title.
 * @param summaryKey The key of the preference summary.
 * @param tag The tag or full class name of the preference.
 * @param preferences The preferences in this screen.
 */
@Suppress("MemberVisibilityCanBePrivate")
open class PreferenceScreen(
    key: String? = null,
    titleKey: String = "${key}_title",
    summaryKey: String? = "${key}_summary",
    tag: String = "PreferenceScreen",
    val preferences: Set<BasePreference>
) : BasePreference(key, titleKey, summaryKey, tag) {

    override fun serialize(ownerDocument: Document, resourceCallback: (BaseResource) -> Unit) =
        super.serialize(ownerDocument, resourceCallback).apply {
            preferences.forEach {
                appendChild(it.serialize(ownerDocument, resourceCallback))
            }
        }
}
