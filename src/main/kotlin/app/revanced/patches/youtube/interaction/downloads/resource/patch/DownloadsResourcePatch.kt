package app.revanced.patches.youtube.interaction.downloads.resource.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.ResourceData
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.impl.ResourcePatch
import app.revanced.patches.youtube.interaction.downloads.annotation.DownloadsCompatibility
import app.revanced.patches.youtube.misc.manifest.patch.FixLocaleConfigErrorPatch
import app.revanced.patches.youtube.misc.playercontrols.resource.patch.BottomControlsResourcePatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.settings.framework.components.impl.StringResource
import app.revanced.patches.youtube.misc.settings.framework.components.impl.SwitchPreference
import app.revanced.util.resources.ResourceUtils
import app.revanced.util.resources.ResourceUtils.Settings.mergeStrings
import app.revanced.util.resources.ResourceUtils.copyResources

@Name("downloads-resource-patch")
@DependsOn([BottomControlsResourcePatch::class, FixLocaleConfigErrorPatch::class, SettingsPatch::class])
@Description("Makes necessary changes to resources for the download button.")
@DownloadsCompatibility
@Version("0.0.1")
class DownloadsResourcePatch : ResourcePatch() {
    override fun execute(data: ResourceData): PatchResult {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(SwitchPreference(
            "revanced_downloads",
            StringResource("revanced_downloads_enabled_title", "Show download button"),
            true,
            StringResource("revanced_downloads_enabled_summary_on", "Download button is visible"),
            StringResource("revanced_downloads_enabled_summary_off", "Download button is hidden")
        ))

        /*
         * Copy strings
         */

        data.mergeStrings("downloads/host/values/strings.xml")

        /*
         * Copy resources
         */

        data.copyResources("downloads", ResourceUtils.ResourceGroup("drawable", "revanced_yt_download_button.xml"))

        /*
        * Add download button node
         */

        BottomControlsResourcePatch.addControls("downloads/host/layout/${BottomControlsResourcePatch.TARGET_RESOURCE_NAME}")

        return PatchResultSuccess()
    }
}