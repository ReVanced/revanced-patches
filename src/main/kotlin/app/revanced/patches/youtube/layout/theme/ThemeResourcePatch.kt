package app.revanced.patches.youtube.layout.theme

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.InputType
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.shared.misc.settings.preference.TextPreference
import app.revanced.patches.youtube.layout.theme.ThemeBytecodePatch.darkThemeBackgroundColor
import app.revanced.patches.youtube.layout.theme.ThemeBytecodePatch.lightThemeBackgroundColor
import app.revanced.patches.youtube.misc.playservice.VersionCheckPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import org.w3c.dom.Element

@Patch(
    dependencies = [
        SettingsPatch::class,
        ResourceMappingPatch::class,
        AddResourcesPatch::class,
        VersionCheckPatch::class,
    ],
)
internal object ThemeResourcePatch : ResourcePatch() {
    private const val SPLASH_BACKGROUND_COLOR = "revanced_splash_background_color"

    override fun execute(context: ResourceContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.SEEKBAR.addPreferences(
            SwitchPreference("revanced_seekbar_custom_color"),
            TextPreference("revanced_seekbar_custom_color_value", inputType = InputType.TEXT_CAP_CHARACTERS),
        )

        // Edit theme colors via resources.
        context.xmlEditor["res/values/colors.xml"].use { editor ->
            val document = editor.file

            val resourcesNode = document.getElementsByTagName("resources").item(0) as Element

            val children = resourcesNode.childNodes
            for (i in 0 until children.length) {
                val node = children.item(i) as? Element ?: continue

                node.textContent =
                    when (node.getAttribute("name")) {
                        "yt_black0", "yt_black1", "yt_black1_opacity95", "yt_black1_opacity98", "yt_black2", "yt_black3",
                        "yt_black4", "yt_status_bar_background_dark", "material_grey_850",
                        -> darkThemeBackgroundColor ?: continue

                        "yt_white1", "yt_white1_opacity95", "yt_white1_opacity98",
                        "yt_white2", "yt_white3", "yt_white4",
                        -> lightThemeBackgroundColor ?: continue

                        else -> continue
                    }
            }
        }

        // Add a dynamic background color to the colors.xml file.
        lightThemeBackgroundColor?.let {
            addColorResource(context, "res/values/colors.xml", SPLASH_BACKGROUND_COLOR, it)
        }

        darkThemeBackgroundColor?.let {
            addColorResource(context, "res/values-night/colors.xml", SPLASH_BACKGROUND_COLOR, it)
        }

        // Edit splash screen files and change the background color,
        // if the background colors are set.
        if (darkThemeBackgroundColor != null && lightThemeBackgroundColor != null) {
            val splashScreenResourceFiles =
                listOf(
                    "res/drawable/quantum_launchscreen_youtube.xml",
                    "res/drawable-sw600dp/quantum_launchscreen_youtube.xml",
                )

            splashScreenResourceFiles.forEach editSplashScreen@{ resourceFile ->
                context.xmlEditor[resourceFile].use { editor ->
                    val document = editor.file

                    val layerList = document.getElementsByTagName("layer-list").item(0) as Element

                    val childNodes = layerList.childNodes
                    for (i in 0 until childNodes.length) {
                        val node = childNodes.item(i)
                        if (node is Element && node.hasAttribute("android:drawable")) {
                            node.setAttribute("android:drawable", "@color/$SPLASH_BACKGROUND_COLOR")
                            return@editSplashScreen
                        }
                    }

                    throw PatchException("Failed to modify launch screen")
                }
            }

            // Fix the splash screen dark mode background color.
            // In earlier versions of the app this is white and makes no sense for dark mode.
            // This is only required for 19.32 and greater, but is applied to all targets.
            // Only dark mode needs this fix as light mode correctly uses the custom color.
            context.xmlEditor["res/values-night/styles.xml"].use { editor ->
                val document = editor.file

                // Create a night mode specific override for the splash screen background.
                val style = document.createElement("style")
                style.setAttribute("name", "Theme.YouTube.Home")
                style.setAttribute("parent", "@style/Base.V23.Theme.YouTube.Home")

                val windowItem = document.createElement("item")
                windowItem.setAttribute("name", "android:windowBackground")
                windowItem.textContent = "@color/$SPLASH_BACKGROUND_COLOR"
                style.appendChild(windowItem)

                val resourcesNode = document.getElementsByTagName("resources").item(0) as Element
                resourcesNode.appendChild(style)
            }
        }
    }

    @Suppress("SameParameterValue")
    private fun addColorResource(
        context: ResourceContext,
        resourceFile: String,
        colorName: String,
        colorValue: String,
    ) {
        context.xmlEditor[resourceFile].use { editor ->
            val document = editor.file

            val resourcesNode = document.getElementsByTagName("resources").item(0) as Element

            resourcesNode.appendChild(
                document.createElement("color").apply {
                    setAttribute("name", colorName)
                    setAttribute("category", "color")
                    textContent = colorValue
                },
            )
        }
    }
}
