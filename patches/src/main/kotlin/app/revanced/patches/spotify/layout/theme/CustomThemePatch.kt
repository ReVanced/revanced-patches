package app.revanced.patches.spotify.layout.theme

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.booleanOption
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.patches.spotify.misc.extension.sharedExtensionPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import org.w3c.dom.Element

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/spotify/layout/theme/CustomThemePatch;"

private val customThemeBytecodePatch = bytecodePatch {
    dependsOn(sharedExtensionPatch)

    execute {
        val colorSpaceUtilsClassDef = colorSpaceUtilsClassFingerprint.originalClassDef

        // Hook a util method that converts ARGB to RGBA in the sRGB color space to replace hardcoded accent colors.
        convertArgbToRgbaFingerprint.match(colorSpaceUtilsClassDef).method.apply {
            addInstructions(
                0,
                """
                    long-to-int p0, p0
                    invoke-static { p0 }, $EXTENSION_CLASS_DESCRIPTOR->replaceColor(I)I
                    move-result p0
                    int-to-long p0, p0
                """
            )
        }

        // Lottie JSON parser method. It parses the JSON Lottie animation into its own class,
        // including the solid color of it.
        parseLottieJsonFingerprint.method.apply {
            val invokeParseColorIndex = indexOfFirstInstructionOrThrow {
                val reference = getReference<MethodReference>()
                reference?.definingClass == "Landroid/graphics/Color;"
                        && reference.name == "parseColor"
            }
            val parsedColorRegister = getInstruction<OneRegisterInstruction>(invokeParseColorIndex + 1).registerA

            val replaceColorDescriptor =  "$EXTENSION_CLASS_DESCRIPTOR->replaceColor(I)I"

            addInstructions(
                invokeParseColorIndex + 2,
                """
                    # Use invoke-static/range because the register number is too large.
                    invoke-static/range { v$parsedColorRegister .. v$parsedColorRegister }, $replaceColorDescriptor
                    move-result v$parsedColorRegister
                """
            )
        }

        // Lottie animated color parser.
        parseAnimatedColorFingerprint.method.apply {
            val invokeArgbIndex = indexOfFirstInstructionOrThrow {
                val reference = getReference<MethodReference>()
                reference?.definingClass == "Landroid/graphics/Color;"
                        && reference.name == "argb"
            }
            val argbColorRegister = getInstruction<OneRegisterInstruction>(invokeArgbIndex + 1).registerA

            addInstructions(
                invokeArgbIndex + 2,
                """
                    invoke-static { v$argbColorRegister }, $EXTENSION_CLASS_DESCRIPTOR->replaceColor(I)I
                    move-result v$argbColorRegister
                """
            )
        }
    }
}

@Suppress("unused")
val customThemePatch = resourcePatch(
    name = "Custom theme",
    description = "Applies a custom theme (defaults to amoled black)",
    use = false,
) {
    compatibleWith("com.spotify.music")

    dependsOn(customThemeBytecodePatch)

    val backgroundColor by stringOption(
        key = "backgroundColor",
        default = "@android:color/black",
        title = "Primary background color",
        description = "The background color. Can be a hex color or a resource reference.",
        required = true,
    )

    val overridePlayerGradientColor by booleanOption(
        key = "overridePlayerGradientColor",
        default = false,
        title = "Override player gradient color",
        description =
            "Apply primary background color to the player gradient color, which changes dynamically with the song.",
        required = false,
    )

    val backgroundColorSecondary by stringOption(
        key = "backgroundColorSecondary",
        default = "#FF121212",
        title = "Secondary background color",
        description = "The secondary background color. (e.g. playlist list in home, player artist, song credits). " +
                "Can be a hex color or a resource reference.\",",
        required = true,
    )

    val accentColor by stringOption(
        key = "accentColor",
        default = "#FF1ED760",
        title = "Accent color",
        description = "The accent color ('Spotify green' by default). Can be a hex color or a resource reference.",
        required = true,
    )

    val accentColorPressed by stringOption(
        key = "accentColorPressed",
        default = "#FF1ABC54",
        title = "Pressed accent color",
        description = "The color when accented buttons are pressed, by default slightly darker than accent. " +
                "Can be a hex color or a resource reference.",
        required = true,
    )

    execute {
        document("res/values/colors.xml").use { document ->
            val resourcesNode = document.getElementsByTagName("resources").item(0) as Element

            val childNodes = resourcesNode.childNodes
            for (i in 0 until childNodes.length) {
                val node = childNodes.item(i) as? Element ?: continue
                val name = node.getAttribute("name")

                // Skip overriding song/player gradient start color if the option is disabled.
                // Gradient end color should be themed regardless to allow the gradient to connect with
                // our primary background color.
                if (name == "bg_gradient_start_color" && !overridePlayerGradientColor!!) {
                    continue
                }

                node.textContent = when (name) {
                    // Main background color.
                    "gray_7",
                    // Left sidebar background color in tablet mode.
                    "gray_10",
                    // Gradient next to user photo and "All" in home page.
                    "dark_base_background_base",
                    // "Add account", "Settings and privacy", "View Profile" left sidebar background color.
                    "dark_base_background_elevated_base",
                    // Song/player gradient start/end color.
                    "bg_gradient_start_color", "bg_gradient_end_color",
                    // Login screen background color and gradient start.
                    "sthlm_blk", "sthlm_blk_grad_start",
                    // Misc.
                    "image_placeholder_color",
                        -> backgroundColor

                    // "About the artist" background color in song player.
                    "gray_15",
                    // Track credits, merch background color in song player.
                    "track_credits_card_bg", "benefit_list_default_color", "merch_card_background",
                    // Playlist list background in home page.
                    "opacity_white_10",
                    // "What's New" pills background.
                    "dark_base_background_tinted_highlight"
                        -> backgroundColorSecondary

                    "dark_brightaccent_background_base",
                    "dark_base_text_brightaccent",
                    "green_light",
                    "spotify_green_157"
                        -> accentColor

                    "dark_brightaccent_background_press"
                        -> accentColorPressed

                    else -> continue
                }
            }
        }

        // Login screen gradient.
        document("res/drawable/start_screen_gradient.xml").use { document ->
            val gradientNode = document.getElementsByTagName("gradient").item(0) as Element

            gradientNode.setAttribute("android:startColor", "@color/gray_7")
            gradientNode.setAttribute("android:endColor", "@color/gray_7")
        }
    }
}
