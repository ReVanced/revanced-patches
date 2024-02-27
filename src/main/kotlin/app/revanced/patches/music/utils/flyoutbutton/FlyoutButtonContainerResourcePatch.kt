package app.revanced.patches.music.utils.flyoutbutton

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources

object FlyoutButtonContainerResourcePatch : ResourcePatch() {
    override fun execute(context: ResourceContext) {

        /**
         * create directory for flyout button container
         */
        context["res/layout-v21"].mkdirs()

        arrayOf(
            ResourceGroup(
                "layout-v21",
                "music_menu_like_buttons.xml"
            )
        ).forEach { resourceGroup ->
            context.copyResources("music/flyout", resourceGroup)
        }

        fun copyResources(resourceGroups: List<ResourceGroup>) {
            resourceGroups.forEach { context.copyResources("music/flyout", it) }
        }

        val resourceFileNames = arrayOf(
            "yt_outline_play_arrow_half_circle_black_24"
        ).map { "$it.png" }.toTypedArray()

        fun createGroup(directory: String) = ResourceGroup(
            directory, *resourceFileNames
        )

        arrayOf("xxxhdpi", "xxhdpi", "xhdpi", "hdpi", "mdpi")
            .map { "drawable-$it" }
            .map(::createGroup)
            .let(::copyResources)

    }
}