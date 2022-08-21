package app.revanced.patches.youtube.misc.settings.framework.components.impl

import app.revanced.patches.youtube.misc.settings.framework.components.BasePreference

/**
 * Switch preference.
 *
 * @param key The key of the switch.
 * @param title The title of the switch.
 * @param default The default value of the switch.
 * @param summaryOn The summary to show when the preference is enabled.
 * @param summaryOff The summary to show when the preference is disabled.
 */
internal class SwitchPreference(
    key: String, title: StringResource,
    val default: Boolean = false,
    var summaryOn: StringResource? = null,
    var summaryOff: StringResource? = null
) : BasePreference(key, title) {
    override val tag: String = "SwitchPreference"
}