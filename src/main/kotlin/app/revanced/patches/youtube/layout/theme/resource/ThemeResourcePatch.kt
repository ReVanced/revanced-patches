package app.revanced.patches.youtube.layout.theme.resource

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.shared.mapping.misc.patch.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.InputType
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.TextPreference
import app.revanced.patches.youtube.layout.seekbar.resource.SeekbarPreferencesPatch
import app.revanced.patches.youtube.layout.theme.bytecode.patch.ThemeBytecodePatch.Companion.darkThemeBackgroundColor
import app.revanced.patches.youtube.layout.theme.bytecode.patch.ThemeBytecodePatch.Companion.lightThemeBackgroundColor
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import org.w3c.dom.Element

@DependsOn([SettingsPatch::class, ResourceMappingPatch::class, SeekbarPreferencesPatch::class])
class ThemeResourcePatch : ResourcePatch {
    override fun execute(context: ResourceContext): PatchResult {
        SeekbarPreferencesPatch.addPreferences(
            TextPreference(
                "revanced_seekbar_color",
                StringResource("revanced_seekbar_color_title", "Seekbar color"),
                StringResource("revanced_seekbar_color_summary", "The color of the seekbar"),
                InputType.TEXT_CAP_CHARACTERS
            )
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

        // Add a dynamic background color to the colors.xml file.
        addResourceColor(context, "res/values/colors.xml",
            SPLASH_BACKGROUND_COLOR, lightThemeBackgroundColor!!)
        addResourceColor(context, "res/values-night/colors.xml",
            SPLASH_BACKGROUND_COLOR, darkThemeBackgroundColor!!)

        // Edit splash screen files and change the background color.
        val splashScreenResourceFiles = listOf(
            "res/drawable/quantum_launchscreen_youtube.xml",
            "res/drawable-sw600dp/quantum_launchscreen_youtube.xml")

         splashScreenResourceFiles.forEach editSplashScreen@ { resourceFile ->
            context.xmlEditor[resourceFile].use {
                val layerList = it.file.getElementsByTagName("layer-list").item(0) as Element

                val childNodes = layerList.childNodes
                for (i in 0 until childNodes.length) {
                    val node = childNodes.item(i)
                    if (node is Element && node.hasAttribute("android:drawable")) {
                        node.setAttribute("android:drawable", "@color/$SPLASH_BACKGROUND_COLOR")
                        return@editSplashScreen
                    }
                }
                return PatchResultError("Failed to modify launch screen")
            }
        }

        return PatchResultSuccess()
    }

    private fun addResourceColor(
        context: ResourceContext,
        resourceFile: String,
        colorName: String,
        colorValue: String
    ) {
        context.xmlEditor[resourceFile].use {
            val resourcesNode = it.file.getElementsByTagName("resources").item(0) as Element

            resourcesNode.appendChild(
                it.file.createElement("color").apply {
                    setAttribute("name", colorName)
                    setAttribute("category", "color")
                    textContent = colorValue
                })
        }
    }

    private companion object {
        private const val SPLASH_BACKGROUND_COLOR = "revanced_splash_background_color"
    }
}