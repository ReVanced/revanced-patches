@file:Suppress("NAME_SHADOWING")

package app.revanced.patches.spotify.layout.theme

import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.patch.stringOption
import org.w3c.dom.Element

@Suppress("unused")
val customThemePatch = resourcePatch(
    name = "Custom theme",
    description = "Applies a custom theme.",
    use = false,
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
        default = "#ff121212",
        title = "Secondary background color",
        description = "The secondary background color. (e.g. playlist list, player arist, credits). Can be a hex color or a resource reference.",
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
        "The color when accented buttons are pressed, by default slightly darker than accent. Can be a hex color or a resource reference.",
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
                        // Gradient next to user photo and "All" in home page
                        "dark_base_background_base",
                        // Main background
                        "gray_7",
                        // Left sidebar background in tablet mode
                        "gray_10",
                        // Add account, Settings and privacy, View Profile left sidebar background
                        "dark_base_background_elevated_base",
                        // Song/player background
                        "bg_gradient_start_color", "bg_gradient_end_color",
                        // Login screen
                        "sthlm_blk", "sthlm_blk_grad_start", "stockholm_black",
                        // Misc
                        "image_placeholder_color",
                        -> backgroundColor

                        // Track credits, merch in song player
                        "track_credits_card_bg", "benefit_list_default_color", "merch_card_background",
                        // Playlist list background in home page
                        "opacity_white_10",
                        // About artist background in song player
                        "gray_15"
                        -> backgroundColorSecondary

                        "dark_brightaccent_background_base", "dark_base_text_brightaccent", "green_light" -> accentColor
                        "dark_brightaccent_background_press" -> accentColorPressed
                        else -> continue
                    }
            }
        }
    }
}
