package app.revanced.patches.youtube.layout.thumbnails

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.strings.AddResourcesPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.copyStrings

@Patch(
    dependencies = [
        SettingsPatch::class,
        AddResourcesPatch::class
    ]
)
internal object AlternativeThumbnailsResourcePatch : ResourcePatch() {
    override fun execute(context: ResourceContext) {
        context.copyStrings("alternativethumbnails/host/values/strings.xml")
    }
}