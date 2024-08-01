package app.revanced.patches.shared.misc.settings.preference

import app.revanced.util.resource.BaseResource
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * Base preference class for all preferences.
 *
 * @param key The key of the preference. If null, other parameters must be specified.
 * @param titleKey The key of the preference title.
 * @param summaryKey The key of the preference summary.
 * @param tag The tag or full class name of the preference.
 */
@Suppress("MemberVisibilityCanBePrivate")
abstract class BasePreference(
    val key: String? = null,
    val titleKey: String = "${key}_title",
    val summaryKey: String? = "${key}_summary",
    val tag: String
) {
    /**
     * Serialize preference element to XML.
     * Overriding methods should invoke super and operate on its return value.
     *
     * @param resourceCallback A callback for additional resources.
     * @param ownerDocument Target document to create elements from.
     *
     * @return The serialized element.
     */
    open fun serialize(ownerDocument: Document, resourceCallback: (BaseResource) -> Unit): Element =
        ownerDocument.createElement(tag).apply {
            key?.let { setAttribute("android:key", it) }
            setAttribute("android:title", "@string/${titleKey}")
            summaryKey?.let { addSummary(it) }
        }

    override fun hashCode(): Int {
        var result = key?.hashCode() ?: 0
        result = 31 * result + titleKey.hashCode()
        result = 31 * result + tag.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BasePreference

        if (key != other.key) return false
        if (titleKey != other.titleKey) return false
        if (tag != other.tag) return false

        return true
    }

    companion object {
        fun Element.addSummary(summaryKey: String, summaryType: SummaryType = SummaryType.DEFAULT) =
            setAttribute("android:${summaryType.type}", "@string/$summaryKey")
    }
}
