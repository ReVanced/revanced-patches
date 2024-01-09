package app.revanced.patches.shared.settings.preference.impl

import app.revanced.patches.shared.settings.preference.BasePreference
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
 */
class SwitchPreference(
    key: String,
    titleKey: String,
    private val summaryOnKey: String,
    private val summaryOffKey: String,
) : BasePreference(key, titleKey, null, "SwitchPreference") {

    /**
     * Initialize using title and summary keys with the suffix "_title", "_summary_on", "_summary_off"
     */
    constructor(key: String) : this(key, "${key}_title",
        "${key}_summary_on", "${key}_summary_off")

    override fun serialize(ownerDocument: Document): Element {
        return super.serialize(ownerDocument).apply {
            addSummary(summaryOnKey, SummaryType.ON)
            addSummary(summaryOffKey, SummaryType.OFF)
        }
    }
}