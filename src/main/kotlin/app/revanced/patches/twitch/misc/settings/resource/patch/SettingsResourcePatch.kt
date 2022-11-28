package app.revanced.patches.twitch.misc.settings.resource.patch

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patches.shared.settings.preference.impl.ArrayResource
import app.revanced.patches.shared.settings.preference.impl.PreferenceScreen
import app.revanced.patches.shared.settings.resource.patch.AbstractSettingsResourcePatch
import app.revanced.patches.twitch.misc.settings.annotations.SettingsCompatibility

@Name("settings-resource-patch")
@SettingsCompatibility
@Version("0.0.1")
class SettingsResourcePatch : AbstractSettingsResourcePatch(
"revanced_prefs",
"twitch/settings"
) {
    internal companion object {
        /* Companion delegates */

        /**
         * Add a new string to the resources.
         *
         * @param identifier The key of the string.
         * @param value The value of the string.
         * @throws IllegalArgumentException if the string already exists.
         */
        fun addString(identifier: String, value: String, formatted: Boolean) =
            AbstractSettingsResourcePatch.addString(identifier, value, formatted)

        /**
         * Add an array to the resources.
         *
         * @param arrayResource The array resource to add.
         */
        fun addArray(arrayResource: ArrayResource) = AbstractSettingsResourcePatch.addArray(arrayResource)

        /**
         * Add a preference to the settings.
         *
         * @param preferenceScreen The name of the preference screen.
         */
        fun addPreferenceScreen(preferenceScreen: PreferenceScreen) = AbstractSettingsResourcePatch.addPreference(preferenceScreen)
    }
}