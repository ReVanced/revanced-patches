package app.revanced.patches.shared.settings.preference.impl

import app.revanced.patches.shared.settings.preference.DefaultBasePreference
import app.revanced.patches.shared.settings.preference.SummaryType
import app.revanced.patches.shared.settings.preference.addSummary
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * A switch preference.
 *
 * @param key The key of the switch.
 * @param titleKey The title of the switch.
 * @param summaryOnKey The summary to show when the preference is enabled.
 * @param summaryOffKey The summary to show when the preference is disabled.
 * @param default The default value of the switch.
 */
class SwitchPreference(
    key: String,
    titleKey: String,
    val summaryOnKey: String,
    val summaryOffKey: String,
    default: Boolean = false,
) : DefaultBasePreference<Boolean>( key, titleKey, null, "SwitchPreference", default) {

    override fun serialize(ownerDocument: Document): Element {
        return super.serialize(ownerDocument).apply {
            addSummary(summaryOnKey, SummaryType.ON)
            addSummary(summaryOffKey, SummaryType.OFF)
        }
    }
}