package app.revanced.patches.youtube.layout.theme.resource

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.shared.mapping.misc.patch.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.InputType
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.TextPreference
import app.revanced.patches.youtube.layout.theme.bytecode.patch.ThemeBytecodePatch.Companion.darkThemeBackgroundColor
import app.revanced.patches.youtube.layout.theme.bytecode.patch.ThemeBytecodePatch.Companion.lightThemeBackgroundColor
import app.revanced.patches.youtube.layout.theme.bytecode.patch.ThemeBytecodePatch.Companion.splashScreenBackgroundColor
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import org.w3c.dom.Element

@DependsOn([SettingsPatch::class, ResourceMappingPatch::class])
class ThemeResourcePatch : ResourcePatch {
    override fun execute(context: ResourceContext): PatchResult {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            TextPreference(
                "revanced_seekbar_color",
                StringResource("revanced_seekbar_color_title", "Seekbar color"),
                StringResource(
                    "revanced_seekbar_color_summary",
                    "The color of the seekbar"
                ),
                InputType.TEXT_CAP_CHARACTERS
            ),
        )

        // Edit theme colors via resources.
        context.xmlEditor["res/values/colors.xml"].use { editor ->
            val resourcesNode = editor.file.getElementsByTagName("resources").item(0) as Element

            val children = resourcesNode.childNodes
            for (i in 0 until children.length) {
                val node = children.item(i) as? Element ?: continue

                node.textContent = when (node.getAttribute("name")) {
                    "yt_black0", "yt_black1", "yt_black1_opacity95", "yt_black1_opacity98", "yt_black2", "yt_black3",
                    "yt_black4", "yt_status_bar_background_dark", "material_grey_850" -> darkThemeBackgroundColor
                        ?: continue

                    "yt_white1", "yt_white1_opacity95", "yt_white1_opacity98",
                    "yt_white2", "yt_white3", "yt_white4",
                    -> lightThemeBackgroundColor ?: continue

                    else -> continue
                }
            }
        }

        splashScreenBackgroundColor ?: return PatchResultSuccess()

        // Edit splash screen background color for Android 11 and below.
        context.xmlEditor["res/values/styles.xml"].use {
            val resourcesNode = it.file.getElementsByTagName("resources").item(0) as Element

            val children = resourcesNode.childNodes
            for (i in 0 until children.length) {
                val node = children.item(i) as? Element ?: continue

                if (node.tagName != "style") continue

                val name = node.getAttribute("name")
                if (name != LAUNCHER_STYLE_NAME) continue

                it.file.createElement("item").apply {
                    setAttribute("name", "android:windowSplashScreenBackground")
                    textContent = splashScreenBackgroundColor
                }.also(node::appendChild)

                break
            }
        }

        // Edit splash screen background color for Android 12+.

        // Add the splash screen background color to the colors.xml file.
        context.xmlEditor["res/values/colors.xml"].use {
            val resourcesNode = it.file.getElementsByTagName("resources").item(0) as Element

            it.file.createElement("color").apply {
                setAttribute("name", COLOR_NAME)
                setAttribute("category", "color")
                textContent = splashScreenBackgroundColor
            }.also(resourcesNode::appendChild)
        }

        // Point to the splash screen background color.
        context.xmlEditor["res/drawable/quantum_launchscreen_youtube.xml"].use {
            val node = it.file.getElementsByTagName("layer-list").item(0) as Element

            val backgroundColorItem = node.childNodes.item(1) as Element
            backgroundColorItem.apply {
                setAttribute("android:drawable", "@color/$COLOR_NAME")
            }
        }

        return PatchResultSuccess()
    }

    private companion object {
        private const val LAUNCHER_STYLE_NAME = "Base.Theme.YouTube.Launcher"
        private const val COLOR_NAME = "splash_background_color"
    }
}