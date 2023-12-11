package app.revanced.patches.youtube.layout.thumbnails

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.mergeStrings

@Patch(
    dependencies = [SettingsPatch::class]
)
internal object AlternativeThumbnailsResourcePatch : ResourcePatch() {
    override fun execute(context: ResourceContext) {
        context.mergeStrings("alternativethumbnails/host/values/strings.xml")
    }
}