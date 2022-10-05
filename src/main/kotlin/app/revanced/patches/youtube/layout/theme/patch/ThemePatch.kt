package app.revanced.patches.youtube.layout.theme.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.*
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.layout.theme.annotations.ThemeCompatibility
import app.revanced.patches.youtube.misc.manifest.patch.FixLocaleConfigErrorPatch
import org.w3c.dom.Element

@Patch
@DependsOn([FixLocaleConfigErrorPatch::class])
@Name("theme")
@Description("Applies a custom theme.")
@ThemeCompatibility
@Version("0.0.1")
class ThemePatch : ResourcePatch {
    override fun execute(context: ResourceContext): PatchResult {
        val darkThemeBackgroundColor = darkThemeBackgroundColor!!
        val lightThemeBackgroundColor = lightThemeBackgroundColor!!

        context.xmlEditor["res/values/colors.xml"].use { editor ->
            val resourcesNode = editor.file.getElementsByTagName("resources").item(0) as Element

            for (i in 0 until resourcesNode.childNodes.length) {
                val node = resourcesNode.childNodes.item(i) as? Element ?: continue

                node.textContent = when (node.getAttribute("name")) {
                    "yt_black1", "yt_black1_opacity95", "yt_black2", "yt_black3", "yt_black4",
                    "yt_status_bar_background_dark" -> darkThemeBackgroundColor

                    "yt_white1", "yt_white1_opacity95", "yt_white2", "yt_white3",
                    "yt_white4" -> lightThemeBackgroundColor

                    else -> continue
                }
            }
        }

        return PatchResultSuccess()
    }

    companion object : OptionsContainer() {
        var darkThemeBackgroundColor: String? by option(
            PatchOption.StringOption(
                key = "darkThemeBackgroundColor",
                default = "@android:color/black",
                title = "Background color for the dark theme",
                description = "The background color of the dark theme. Can be a hex color or a resource reference.",
            )
        )

        var lightThemeBackgroundColor: String? by option(
            PatchOption.StringOption(
                key = "lightThemeBackgroundColor",
                default = "@android:color/white",
                title = "Background color for the light theme",
                description = "The background color of the light theme. Can be a hex color or a resource reference.",
            )
        )
    }
}