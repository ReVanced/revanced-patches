package app.revanced.patches.youtube.layout.returnyoutubedislike.resource.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.youtube.layout.returnyoutubedislike.annotations.ReturnYouTubeDislikeCompatibility
import app.revanced.patches.youtube.misc.manifest.patch.FixLocaleConfigErrorPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.settings.framework.components.impl.Preference
import app.revanced.patches.youtube.misc.settings.framework.components.impl.StringResource
import app.revanced.util.resources.ResourceUtils.Settings.mergeStrings

@DependsOn([FixLocaleConfigErrorPatch::class, SettingsPatch::class])
@Name("return-youtube-dislike-resource-patch")
@Description("Adds the preferences for Return YouTube Dislike.")
@ReturnYouTubeDislikeCompatibility
@Version("0.0.1")
class ReturnYouTubeDislikeResourcePatch : ResourcePatch {
    override fun execute(context: ResourceContext): PatchResult {
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
        context.mergeStrings("returnyoutubedislike/host/values/strings.xml")

        return PatchResultSuccess()
    }
}