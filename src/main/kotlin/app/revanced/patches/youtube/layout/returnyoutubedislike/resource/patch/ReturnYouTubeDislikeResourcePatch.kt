package app.revanced.patches.youtube.layout.returnyoutubedislike.resource.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.ResourceData
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.impl.ResourcePatch
import app.revanced.patches.youtube.layout.returnyoutubedislike.annotations.ReturnYouTubeDislikeCompatibility
import app.revanced.patches.youtube.misc.manifest.patch.FixLocaleConfigErrorPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.settings.framework.components.impl.Preference
import app.revanced.patches.youtube.misc.settings.framework.components.impl.StringResource
import app.revanced.util.resources.ResourceUtils.iterateXmlNodeChildren

@DependsOn([FixLocaleConfigErrorPatch::class, SettingsPatch::class])
@Name("return-youtube-dislike-resource-patch")
@Description("Adds the preferences for Return YouTube Dislike.")
@ReturnYouTubeDislikeCompatibility
@Version("0.0.1")
class ReturnYouTubeDislikeResourcePatch : ResourcePatch() {
    override fun execute(data: ResourceData): PatchResult {
        val youtubePackage = "com.google.android.youtube"
        SettingsPatch.addPreference(
            Preference(
                StringResource("revanced_ryd_settings_title", "Return YouTube Dislike"),
                Preference.Intent(
                    youtubePackage,
                    "ryd_settings",
                    "com.google.android.libraries.social.licenses.LicenseActivity"
                ),
                StringResource("revanced_ryd_settings_summary", "Settings for Return YouTube Dislike"),
            )
        )
        // merge strings
        data.iterateXmlNodeChildren("returnyoutubedislike/host/values/strings.xml", "resources") {
            // TODO: figure out why this is needed
            if (!it.hasAttributes()) return@iterateXmlNodeChildren
            val attributes = it.attributes
            SettingsPatch.addString(attributes.getNamedItem("name")!!.nodeValue!!, it.textContent!!)
        }

        return PatchResultSuccess()
    }
}