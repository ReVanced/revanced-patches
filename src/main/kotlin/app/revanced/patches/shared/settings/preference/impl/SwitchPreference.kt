package app.revanced.patches.shared.settings.preference.impl

import app.revanced.patches.shared.settings.preference.BaseResource
import app.revanced.patches.shared.settings.preference.DefaultBasePreference
import app.revanced.patches.shared.settings.preference.SummaryType
import app.revanced.patches.shared.settings.preference.addSummary
import app.revanced.patches.shared.settings.resource.patch.AbstractSettingsResourcePatch.Companion.include
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * A switch preference.
 *
 * @param key The key of the switch.
 * @param title The title of the switch.
 * @param summaryOn The summary to show when the preference is enabled.
 * @param summaryOff The summary to show when the preference is disabled.
 * @param userDialogMessage The message to show in a dialog when the user toggles the preference.
 * @param default The default value of the switch.
 */
class SwitchPreference(
    key: String, title: StringResource,
    val summaryOn: StringResource,
    val summaryOff: StringResource,
    val userDialogMessage: StringResource? = null,
    default: Boolean = false,
) : DefaultBasePreference<Boolean>( key,  title,  null,  "SwitchPreference", default) {
    override fun serialize(ownerDocument: Document, resourceCallback: (BaseResource) -> Unit): Element {
        userDialogMessage?.include()

        return super.serialize(ownerDocument, resourceCallback).apply {
            addSummary(summaryOn.also { resourceCallback.invoke(it) }, SummaryType.ON)
            addSummary(summaryOff.also { resourceCallback.invoke(it) }, SummaryType.OFF)
        }
    }
}