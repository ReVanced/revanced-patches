package app.revanced.patches.youtube.misc.settings.framework.components

import app.revanced.patches.youtube.misc.settings.framework.components.impl.StringResource

/**
 * Base preference class for all preferences.
 *
 * @param key The key of the preference.
 * @param title The title of the preference.
 */
internal abstract class BasePreference(
    override val key: String,
    override val title: StringResource,
) : IPreference