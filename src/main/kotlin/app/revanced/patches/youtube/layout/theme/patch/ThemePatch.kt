package app.revanced.patches.youtube.layout.theme.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.ResourceData
import app.revanced.patcher.patch.*
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.impl.ResourcePatch
import app.revanced.patches.youtube.layout.theme.annotations.ThemeCompatibility
import app.revanced.patches.youtube.misc.manifest.patch.FixLocaleConfigErrorPatch
import org.w3c.dom.Element

@Patch
@DependsOn([FixLocaleConfigErrorPatch::class])
@Name("theme")
@Description("Enables a custom theme.")
@ThemeCompatibility
@Version("0.0.1")
class ThemePatch : ResourcePatch() {
    override fun execute(data: ResourceData): PatchResult {
        val theme = Themes.of(theme!!) ?: return PatchResultError("Theme '$theme' not found.")

        data.xmlEditor["res/values/colors.xml"].use { editor ->
            val resourcesNode = editor.file.getElementsByTagName("resources").item(0) as Element

            for (i in 0 until resourcesNode.childNodes.length) {
                val node = resourcesNode.childNodes.item(i) as? Element ?: continue
                node.textContent = theme.apply(node.getAttribute("name")) ?: continue
            }
        }

        return PatchResultSuccess()
    }

    companion object : OptionsContainer() {
        var theme: String? by option(
            PatchOption.StringListOption(
                key = "theme",
                default = Themes.Amoled.name,
                options = Themes.names,
                title = "Theme",
                description = "Select a theme.",
                required = true
            )
        )
    }

    enum class Themes(val apply: (String) -> String?) {
        Amoled({ attr ->
            when (attr) {
                "yt_black1", "yt_black1_opacity95", "yt_black2", "yt_black3", "yt_black4",
                "yt_status_bar_background_dark" -> "@android:color/black"

                "yt_selected_nav_label_dark" -> "#ffdf0000"
                else -> null
            }
        });

        companion object {
            val names = values().map { it.name }

            fun of(name: String) = values().firstOrNull { it.name == name }
        }
    }
}