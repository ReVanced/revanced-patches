package app.revanced.patches.spotify.layout.theme

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.spotify.misc.extension.sharedExtensionPatch
import app.revanced.util.containsLiteralInstruction
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstLiteralInstructionOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import org.w3c.dom.Element

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/spotify/layout/theme/CustomThemePatch;"

internal val spotifyBackgroundColor = stringOption(
    key = "backgroundColor",
    default = "@android:color/black",
    title = "Primary background color",
    description = "The background color. Can be a hex color or a resource reference.",
    required = true,
)

internal val spotifyBackgroundColorSecondary = stringOption(
    key = "backgroundColorSecondary",
    default = "#FF121212",
    title = "Secondary background color",
    description =
        "The secondary background color. (e.g. playlist list in home, player artist, song credits). Can be a hex color or a resource reference.",
    required = true,
)

internal val spotifyAccentColor = stringOption(
    key = "accentColor",
    default = "#FF1ED760",
    title = "Accent color",
    description = "The accent color ('Spotify green' by default). Can be a hex color or a resource reference.",
    required = true,
)

internal val spotifyAccentColorPressed = stringOption(
    key = "accentColorPressed",
    default = "#FF169C46",
    title = "Pressed dark theme accent color",
    description =
        "The color when accented buttons are pressed, by default slightly darker than accent. Can be a hex color or a resource reference.",
    required = true,
)

@Suppress("unused")
val customThemePatch = resourcePatch(
    name = "Custom theme",
    description = "Applies a custom theme (defaults to amoled black)",
    use = false,
) {
    compatibleWith("com.spotify.music")

    dependsOn(customThemeBytecodePatch)

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
                    // Gradient next to user photo and "All" in home page.
                    "dark_base_background_base",
                    // Main background.
                    "gray_7",
                    // Left sidebar background in tablet mode.
                    "gray_10",
                    // "Add account", "Settings and privacy", "View Profile" left sidebar background.
                    "dark_base_background_elevated_base",
                    // Song/player background.
                    "bg_gradient_start_color", "bg_gradient_end_color",
                    // Login screen background.
                    "sthlm_blk", "sthlm_blk_grad_start", "stockholm_black",
                    // Misc.
                    "image_placeholder_color",
                        -> backgroundColor

                    // Track credits, merch background in song player.
                    "track_credits_card_bg", "benefit_list_default_color", "merch_card_background",
                    // Playlist list background in home page.
                    "opacity_white_10",
                    // "About the artist" background in song player.
                    "gray_15",
                    // "What's New" pills background.
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

internal val customThemeBytecodePatch = bytecodePatch {
    dependsOn(sharedExtensionPatch)

    execute {
        fun MutableMethod.addColorChangeInstructions(literal: Long, colorString: String) {
            val index = indexOfFirstLiteralInstructionOrThrow(literal)
            val register = getInstruction<OneRegisterInstruction>(index).registerA

            addInstructions(
                index + 1,
                """
                    const-string v$register, "$colorString"
                    invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->getThemeColor(Ljava/lang/String;)J
                    move-result-wide v$register
                """
            )
        }

        val encoreColorsClassName = with(encoreThemeFingerprint) {
            // Find index of the first static get found after the string constant.
            val encoreColorsFieldReferenceIndex = originalMethod.indexOfFirstInstructionOrThrow(
                stringMatches!!.first().index,
                Opcode.SGET_OBJECT
            )

            originalMethod.getInstruction(encoreColorsFieldReferenceIndex)
                .getReference<FieldReference>()!!.definingClass
        }

        val encoreColorsConstructorFingerprint = fingerprint {
            accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
            custom { method, classDef ->
                classDef.type == encoreColorsClassName &&
                        method.containsLiteralInstruction(PLAYLIST_BACKGROUND_COLOR_LITERAL)
            }
        }

        val backgroundColor by spotifyBackgroundColor
        val backgroundColorSecondary by spotifyBackgroundColorSecondary

        encoreColorsConstructorFingerprint.method.apply {
            addColorChangeInstructions(PLAYLIST_BACKGROUND_COLOR_LITERAL, backgroundColor!!)
            addColorChangeInstructions(SHARE_MENU_BACKGROUND_COLOR_LITERAL, backgroundColorSecondary!!)
        }

        homeCategoryPillColorsFingerprint.method.addColorChangeInstructions(
            HOME_CATEGORY_PILL_COLOR_LITERAL,
            backgroundColorSecondary!!
        )

        settingsHeaderColorFingerprint.method.addColorChangeInstructions(
            SETTINGS_HEADER_COLOR_LITERAL,
            backgroundColorSecondary!!
        )
    }
}
