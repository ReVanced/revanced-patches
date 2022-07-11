package app.revanced.patches.youtube.interaction.swipecontrols.patch

import app.revanced.extensions.injectResources
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.ResourceData
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.impl.ResourcePatch
import app.revanced.patches.youtube.interaction.swipecontrols.annotation.SwipeControlsCompatibility

@Name("swipe-controls-resource-patch")
@SwipeControlsCompatibility
@Version("0.0.1")
class SwipeControlsResourcesPatch : ResourcePatch() {
    override fun execute(data: ResourceData): PatchResult {
        val resourcesDir = "swipecontrols"

        data.injectResources(
            this.javaClass.classLoader,
            resourcesDir,
            "drawable",
            listOf(
                "ic_sc_brightness_auto",
                "ic_sc_brightness_manual",
                "ic_sc_volume_mute",
                "ic_sc_volume_normal"
            ).map { "$it.xml" }
        )
        return PatchResultSuccess()
    }
}
