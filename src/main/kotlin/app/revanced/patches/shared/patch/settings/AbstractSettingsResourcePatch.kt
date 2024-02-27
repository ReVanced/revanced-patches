package app.revanced.patches.shared.patch.settings

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources
import app.revanced.util.copyXmlNode

/**
 * Abstract settings resource patch
 *
 * @param sourceDirectory Source directory to copy the preference template from
 */
abstract class AbstractSettingsResourcePatch(
    private val sourceDirectory: String
) : ResourcePatch() {
    override fun execute(context: ResourceContext) {
        val sourceHostDirectory = "$sourceDirectory/host"
        val isYouTube = sourceDirectory.startsWith("youtube")
        /**
         * Copy strings
         */
        context.copyXmlNode(sourceHostDirectory, "values/strings.xml", "resources")

        /**
         * Copy ReVanced Settings
         */
        if (isYouTube) {
            context.copyResources(
                sourceDirectory,
                ResourceGroup("xml", "revanced_prefs.xml")
            )
        }
    }
}