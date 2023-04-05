package app.revanced.patches.shared.settings.preference.impl

import app.revanced.patches.shared.settings.preference.*
import app.revanced.patches.shared.settings.resource.patch.AbstractSettingsResourcePatch.Companion.include
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * Switch preference.
 *
 * @param key The key of the switch.
 * @param title The title of the switch.
 * @param default The default value of the switch.
 * @param summaryOn The summary to show when the preference is enabled.
 * @param summaryOff The summary to show when the preference is disabled.
 * @param userDialogMessage The message to show in a dialog when the user toggles the preference.
 */
internal class SwitchPreference(
    key: String, title: StringResource,
    val default: Boolean = false,
    val summaryOn: StringResource? = null,
    val summaryOff: StringResource? = null,
    val userDialogMessage: StringResource? = null
) : BasePreference(key, title) {
    override val tag: String = "SwitchPreference"

    override fun serialize(ownerDocument: Document, resourceCallback: ((IResource) -> Unit)?): Element {
        // dialog message is stored as a regular string and later referenced by SettingsEnum
        userDialogMessage?.include()

        return super.serialize(ownerDocument, resourceCallback).apply {
            addDefault(default)
            addSummary(summaryOn?.also { resourceCallback?.invoke(it) }, SummaryType.ON)
            addSummary(summaryOff?.also { resourceCallback?.invoke(it) }, SummaryType.OFF)
        }
    }
}