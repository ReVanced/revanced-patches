package app.revanced.patches.youtube.misc.settings.resource.patch

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.ResourceData
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Dependencies
import app.revanced.patcher.patch.impl.ResourcePatch
import app.revanced.patches.youtube.misc.manifest.patch.FixLocaleConfigErrorPatch
import app.revanced.patches.youtube.misc.settings.annotations.SettingsCompatibility
import app.revanced.util.resources.ResourceUtils
import app.revanced.util.resources.ResourceUtils.copyResources
import app.revanced.util.resources.ResourceUtils.copyXmlNode

@Name("settings-resource-patch")
@SettingsCompatibility
@Dependencies([FixLocaleConfigErrorPatch::class])
@Version("0.0.1")
class SettingsResourcePatch : ResourcePatch() {
    override fun execute(data: ResourceData): PatchResult {
        /*
         * Copy strings
         */

        data.copyXmlNode("settings/host", "values/strings.xml", "resources")

        /*
         * Copy arrays
         */

        data.copyXmlNode("settings/host", "values/arrays.xml", "resources")

        /*
         * Copy preference fragments
         */

        data.copyXmlNode("settings/host", "xml/settings_fragment.xml", "PreferenceScreen")

        /*
         * Copy layout resources
         */
        arrayOf(
            ResourceUtils.ResourceGroup(
                "layout",
                "xsettings_toolbar.xml",
                "xsettings_with_toolbar.xml",
                "xsettings_with_toolbar_layout.xml"
            ),
            ResourceUtils.ResourceGroup(
                "xml",
                "revanced_prefs.xml"
            )
        ).forEach { resourceGroup ->
            data.copyResources("settings", resourceGroup)
        }

        return PatchResultSuccess()
    }
}