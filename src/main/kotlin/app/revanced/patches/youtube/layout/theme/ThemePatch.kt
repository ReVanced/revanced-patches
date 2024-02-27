package app.revanced.patches.youtube.layout.theme

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.stringPatchOption
import app.revanced.patches.youtube.layout.theme.GeneralThemePatch.isMonetPatchIncluded
import app.revanced.patches.youtube.utils.settings.ResourceUtils.updatePatchStatusTheme
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import org.w3c.dom.Element

@Patch(
    name = "Theme",
    description = "Change the app's theme to the values specified in options.json.",
    dependencies = [
        GeneralThemePatch::class,
        SettingsPatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.25.40",
                "18.27.36",
                "18.29.38",
                "18.30.37",
                "18.31.40",
                "18.32.39",
                "18.33.40",
                "18.34.38",
                "18.35.36",
                "18.36.39",
                "18.37.36",
                "18.38.44",
                "18.39.41",
                "18.40.34",
                "18.41.39",
                "18.42.41",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.46.45",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39"
            ]
        )
    ]
)
@Suppress("unused")
object ThemePatch : ResourcePatch() {
    private const val AMOLED_BLACK_COLOR = "@android:color/black"
    private const val CATPPUCCIN_MOCHA_COLOR = "#FF181825"
    private const val DARK_PINK_COLOR = "#FF290025"
    private const val DARK_BLUE_COLOR = "#FF001029"
    private const val DARK_GREEN_COLOR = "#FF002905"
    private const val DARK_YELLOW_COLOR = "#FF282900"
    private const val DARK_ORANGE_COLOR = "#FF291800"
    private const val DARK_RED_COLOR = "#FF290000"

    private const val WHITE_COLOR = "@android:color/white"
    private const val CATPPUCCIN_LATTE_COLOR = "#FFE6E9EF"
    private const val LIGHT_PINK_COLOR = "#FFFCCFF3"
    private const val LIGHT_BLUE_COLOR = "#FFD1E0FF"
    private const val LIGHT_GREEN_COLOR = "#FFCCFFCC"
    private const val LIGHT_YELLOW_COLOR = "#FFFDFFCC"
    private const val LIGHT_ORANGE_COLOR = "#FFFFE6CC"
    private const val LIGHT_RED_COLOR = "#FFFFD6D6"

    private val DarkThemeBackgroundColor by stringPatchOption(
        key = "DarkThemeBackgroundColor",
        default = AMOLED_BLACK_COLOR,
        values = mapOf(
            "Amoled Black" to AMOLED_BLACK_COLOR,
            "Catppuccin (Mocha)" to CATPPUCCIN_MOCHA_COLOR,
            "Dark Pink" to DARK_PINK_COLOR,
            "Dark Blue" to DARK_BLUE_COLOR,
            "Dark Green" to DARK_GREEN_COLOR,
            "Dark Yellow" to DARK_YELLOW_COLOR,
            "Dark Orange" to DARK_ORANGE_COLOR,
            "Dark Red" to DARK_RED_COLOR
        ),
        title = "Dark theme background color",
        description = "Can be a hex color (#AARRGGBB) or a color resource reference.",
        required = true
    )

    private val LightThemeBackgroundColor by stringPatchOption(
        key = "LightThemeBackgroundColor",
        default = WHITE_COLOR,
        values = mapOf(
            "White" to WHITE_COLOR,
            "Catppuccin (Latte)" to CATPPUCCIN_LATTE_COLOR,
            "Light Pink" to LIGHT_PINK_COLOR,
            "Light Blue" to LIGHT_BLUE_COLOR,
            "Light Green" to LIGHT_GREEN_COLOR,
            "Light Yellow" to LIGHT_YELLOW_COLOR,
            "Light Orange" to LIGHT_ORANGE_COLOR,
            "Light Red" to LIGHT_RED_COLOR
        ),
        title = "Light theme background color",
        description = "Can be a hex color (#AARRGGBB) or a color resource reference.",
    )

    private fun getThemeString(
        darkThemeColor: String,
        lightThemeColor: String
    ): String {
        return if (lightThemeColor != WHITE_COLOR)
            getLightThemeString(lightThemeColor) + " + " + getDarkThemeString(darkThemeColor)
        else
            getDarkThemeString(darkThemeColor)
    }

    private fun getDarkThemeString(darkThemeColor: String) =
        when (darkThemeColor) {
            AMOLED_BLACK_COLOR -> "Amoled Black"
            CATPPUCCIN_MOCHA_COLOR -> "Catppuccin (Mocha)"
            DARK_PINK_COLOR -> "Dark Pink"
            DARK_BLUE_COLOR -> "Dark Blue"
            DARK_GREEN_COLOR -> "Dark Green"
            DARK_YELLOW_COLOR -> "Dark Yellow"
            DARK_ORANGE_COLOR -> "Dark Orange"
            DARK_RED_COLOR -> "Dark Red"
            else -> "Custom"
        }

    private fun getLightThemeString(lightThemeColor: String) =
        when (lightThemeColor) {
            CATPPUCCIN_LATTE_COLOR -> "Catppuccin (Latte)"
            LIGHT_PINK_COLOR -> "Light Pink"
            LIGHT_BLUE_COLOR -> "Light Blue"
            LIGHT_GREEN_COLOR -> "Light Green"
            LIGHT_YELLOW_COLOR -> "Light Yellow"
            LIGHT_ORANGE_COLOR -> "Light Orange"
            LIGHT_RED_COLOR -> "Light Red"
            else -> "Custom"
        }

    override fun execute(context: ResourceContext) {

        val darkThemeColor = DarkThemeBackgroundColor
            ?: throw PatchException("Invalid dark color.")

        val lightThemeColor = LightThemeBackgroundColor
            ?: throw PatchException("Invalid light color.")

        arrayOf("values", "values-v31").forEach { path ->
            context.xmlEditor["res/$path/colors.xml"].use { editor ->
                val resourcesNode = editor.file.getElementsByTagName("resources").item(0) as Element

                for (i in 0 until resourcesNode.childNodes.length) {
                    val node = resourcesNode.childNodes.item(i) as? Element ?: continue

                    node.textContent = when (node.getAttribute("name")) {
                        "yt_black0", "yt_black1", "yt_black1_opacity95", "yt_black1_opacity98", "yt_black2", "yt_black3",
                        "yt_black4", "yt_status_bar_background_dark", "material_grey_850" -> darkThemeColor

                        else -> continue
                    }
                }
            }
        }

        context.xmlEditor["res/values/colors.xml"].use { editor ->
            val resourcesNode = editor.file.getElementsByTagName("resources").item(0) as Element

            val children = resourcesNode.childNodes
            for (i in 0 until children.length) {
                val node = children.item(i) as? Element ?: continue

                node.textContent = when (node.getAttribute("name")) {
                    "yt_white1", "yt_white1_opacity95", "yt_white1_opacity98",
                    "yt_white2", "yt_white3", "yt_white4",
                    -> lightThemeColor

                    else -> continue
                }
            }
        }

        val currentTheme = if (isMonetPatchIncluded)
            "MaterialYou + " + getThemeString(darkThemeColor, lightThemeColor)
        else
            getThemeString(darkThemeColor, lightThemeColor)

        context.updatePatchStatusTheme(currentTheme)

    }
}
