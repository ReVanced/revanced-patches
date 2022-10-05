package app.revanced.patches.spotify.layout.theme.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.*
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.spotify.layout.theme.annotations.ThemeCompatibility
import app.revanced.patches.youtube.misc.manifest.patch.FixLocaleConfigErrorPatch
import org.w3c.dom.Element

@Patch
@DependsOn([FixLocaleConfigErrorPatch::class])
@Name("spotify-theme")
@Description("Applies a custom theme.")
@ThemeCompatibility
@Version("0.0.1")
class ThemePatch : ResourcePatch {
    override fun execute(context: ResourceContext): PatchResult {
        context.xmlEditor["res/values/colors.xml"].use { editor ->
            val resourcesNode = editor.file.getElementsByTagName("resources").item(0) as Element

            for (i in 0 until resourcesNode.childNodes.length) {
                val node = resourcesNode.childNodes.item(i) as? Element ?: continue

                node.textContent = when (node.getAttribute("name")) {
                    "gray_7" -> backgroundColor!!
                    "dark_brightaccent_background_base", "dark_base_text_brightaccent", "green_light" -> accentColor!!
                    "dark_brightaccent_background_press" -> accentPressedColor!!
                    else -> continue
                }
            }
        }

        return PatchResultSuccess()
    }

    companion object : OptionsContainer() {
        var backgroundColor: String? by option(
            PatchOption.StringOption(
                key = "backgroundColor",
                default = "@android:color/black",
                title = "Background color",
                description = "The background color. Can be a hex color or a resource reference.",
            )
        )
        var accentColor: String? by option(
            PatchOption.StringOption(
                key = "accentColor",
                default = "#ff1ed760",
                title = "Accent color",
                description = "The accent color ('spotify green' by default). Can be a hex color or a resource reference.",
            )
        )
        var accentPressedColor: String? by option(
            PatchOption.StringOption(
                key = "accentPressedColor",
                default = "#ff169c46",
                title = "Pressed accent for the dark theme",
                description = "The color when accented buttons are pressed, by default slightly darker than accent. Can be a hex color or a resource reference.",
            )
        )
    }
}