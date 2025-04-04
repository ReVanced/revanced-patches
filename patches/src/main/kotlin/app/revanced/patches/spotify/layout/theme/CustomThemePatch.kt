package app.revanced.patches.spotify.layout.theme

import app.revanced.patcher.patch.resourcePatch
import org.w3c.dom.Element

@Suppress("unused")
val customThemePatch = resourcePatch(
    name = "Custom theme",
    description = "Applies a custom theme (defaults to amoled black)",
    use = false,
) {
    compatibleWith("com.spotify.music")

    dependsOn(customThemeByteCodePatch)

    val backgroundColor by spotifyBackgroundColor()
    val backgroundColorSecondary by spotifyBackgroundColorSecondary()
    val accentColor by spotifyAccentColor()
    val accentColorPressed by spotifyAccentColorPressed()

    execute {
        document("res/values/colors.xml").use { document ->
            val resourcesNode = document.getElementsByTagName("resources").item(0) as Element

            val childNodes = resourcesNode.childNodes
            for (i in 0 until childNodes.length) {
                val node = childNodes.item(i) as? Element ?: continue

                node.textContent = when (node.getAttribute("name")) {
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
                    "gray_15",
                    // What's New pills background
                    "dark_base_background_tinted_highlight"
                        -> backgroundColorSecondary

                    "dark_brightaccent_background_base", "dark_base_text_brightaccent", "green_light" -> accentColor
                    "dark_brightaccent_background_press" -> accentColorPressed
                    else -> continue
                }
            }
        }
    }
}
