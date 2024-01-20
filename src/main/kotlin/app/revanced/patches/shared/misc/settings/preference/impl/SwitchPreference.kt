package app.revanced.patches.shared.misc.settings.preference.impl

import app.revanced.patches.shared.misc.settings.preference.BasePreference
import app.revanced.patches.shared.misc.settings.preference.SummaryType
import app.revanced.util.resource.BaseResource
import org.w3c.dom.Document

/**
 * A switch preference.
 */
@Suppress("MemberVisibilityCanBePrivate")
class SwitchPreference : BasePreference {
    val summaryOnKey: String
    val summaryOffKey: String

    constructor(
        key: String? = null,
        titleKey: String,
        summaryOnKey: String,
        summaryOffKey: String,
    ) : super(key, titleKey, null, "SwitchPreference") {
        this.summaryOnKey = summaryOnKey
        this.summaryOffKey = summaryOffKey
    }

    constructor(
        key: String,
    ) : super(key, "SwitchPreference") {
        this.summaryOnKey = "${key}_summary_on"
        this.summaryOffKey = "${key}_summary_off"
    }

    override fun serialize(ownerDocument: Document, resourceCallback: (BaseResource) -> Unit) =
        super.serialize(ownerDocument, resourceCallback).apply {
            addSummary(summaryOnKey, SummaryType.ON)
            addSummary(summaryOffKey, SummaryType.OFF)
        }
}
