package app.revanced.patches.youtube.layout.seekbar

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import org.w3c.dom.Element

@Patch(dependencies = [SettingsPatch::class, ResourceMappingPatch::class])
internal object SeekbarColorResourcePatch : ResourcePatch() {
    internal var reelTimeBarPlayedColorId = -1L
    internal var inlineTimeBarColorizedBarPlayedColorDarkId = -1L
    internal var inlineTimeBarPlayedNotHighlightedColorId = -1L

    override fun execute(context: ResourceContext) {
        reelTimeBarPlayedColorId = ResourceMappingPatch[
            "color",
            "reel_time_bar_played_color",
        ]
        inlineTimeBarColorizedBarPlayedColorDarkId = ResourceMappingPatch[
            "color",
            "inline_time_bar_colorized_bar_played_color_dark",
        ]
        inlineTimeBarPlayedNotHighlightedColorId = ResourceMappingPatch[
            "color",
            "inline_time_bar_played_not_highlighted_color",
        ]

        // Edit the resume playback drawable and replace the progress bar with a custom drawable
        context.xmlEditor["res/drawable/resume_playback_progressbar_drawable.xml"].use { editor ->
            val document = editor.file

            val layerList = document.getElementsByTagName("layer-list").item(0) as Element
            val progressNode = layerList.getElementsByTagName("item").item(1) as Element
            if (!progressNode.getAttributeNode("android:id").value.endsWith("progress")) {
                throw PatchException("Could not find progress bar")
            }
            val scaleNode = progressNode.getElementsByTagName("scale").item(0) as Element
            val shapeNode = scaleNode.getElementsByTagName("shape").item(0) as Element
            val replacementNode = document.createElement(
                "app.revanced.integrations.youtube.patches.theme.ProgressBarDrawable",
            )
            scaleNode.replaceChild(replacementNode, shapeNode)
        }
    }
}
