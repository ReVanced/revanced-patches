package app.revanced.patches.shared.settings.preference.impl

import app.revanced.patches.shared.settings.preference.BasePreference
import app.revanced.patches.shared.settings.preference.addSummary
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * A non interactive preference.
 *
 * Not backed by any preference key/value,
 * and cannot be changed by or interacted with by the user.
 *
 * @param titleKey The title of the preference.
 * @param summaryKey The summary of the text preference.
 * @param selectable If this preference responds to tapping.
 *                   Setting to 'true' restores the horizontal dividers on the top and bottom,
 *                   but tapping will still do nothing since this Preference has no key.
 */
class NonInteractivePreference(
    titleKey: String,
    summaryKey: String?,
    tag: String = "Preference",
    // If androidx.preference is later used, this can be changed to the show top/bottom dividers feature.
    val selectable: Boolean = false
) : BasePreference(null, titleKey, summaryKey, tag) {
    override fun serialize(ownerDocument: Document): Element {
        return super.serialize(ownerDocument).apply {
            addSummary(summaryKey?.also {
                setAttribute("android:selectable", selectable.toString())
            })
        }
    }
}