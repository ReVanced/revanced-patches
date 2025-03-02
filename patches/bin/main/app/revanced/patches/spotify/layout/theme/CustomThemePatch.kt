@file:Suppress("NAME_SHADOWING")

package app.revanced.patches.spotify.layout.theme

import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.patch.stringOption
import org.w3c.dom.Element

@Suppress("unused")
val customThemePatch = resourcePatch(
    name = "Custom theme",
    description = "Applies a custom theme.",
) {
    compatibleWith("com.spotify.music")

    val backgroundColor by stringOption(
        key = "backgroundColor",
        default = "@android:color/black",
        title = "Primary background color",
        description = "The background color. Can be a hex color or a resource reference.",
        required = true,
    )

    val backgroundColorSecondary by stringOption(
        key = "backgroundColorSecondary",
        default = "#ff282828",
        title = "Secondary background color",
        description = "The secondary background color. (e.g. search box, artist & podcast). Can be a hex color or a resource reference.",
        required = true,
    )

    val accentColor by stringOption(
        key = "accentColor",
        default = "#ff1ed760",
        title = "Accent color",
        description = "The accent color ('Spotify green' by default). Can be a hex color or a resource reference.",
        required = true,
    )

    val accentColorPressed by stringOption(
        key = "accentColorPressed",
        default = "#ff169c46",
        title = "Pressed dark theme accent color",
        description =
        "The color when accented buttons are pressed, by default slightly darker than accent. " +
            "Can be a hex color or a resource reference.",
        required = true,
    )

    execute {
        val backgroundColor = backgroundColor!!
        val backgroundColorSecondary = backgroundColorSecondary!!
        val accentColor = accentColor!!
        val accentColorPressed = accentColorPressed!!

        document("res/values/colors.xml").use { document ->
            val resourcesNode = document.getElementsByTagName("resources").item(0) as Element

            val childNodes = resourcesNode.childNodes
            for (i in 0 until childNodes.length) {
                val node = childNodes.item(i) as? Element ?: continue

                node.textContent =
                    when (node.getAttribute("name")) {
                        "dark_base_background_elevated_base", "design_dark_default_color_background",
                        "design_dark_default_color_surface", "gray_7", "gray_background", "gray_layer",
                        "sthlm_blk",
                        -> backgroundColor

                        "gray_15" -> backgroundColorSecondary

                        "dark_brightaccent_background_base", "dark_base_text_brightaccent", "green_light" -> accentColor

                        "dark_brightaccent_background_press" -> accentColorPressed
                        else -> continue
                    }
            }
        }
    }
}
