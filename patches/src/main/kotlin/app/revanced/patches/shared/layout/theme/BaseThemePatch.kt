package app.revanced.patches.shared.layout.theme

import app.revanced.patcher.patch.BytecodePatchBuilder
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.util.childElementsSequence
import java.util.logging.Logger

internal const val PURE_BLACK_COLOR = "@android:color/black"
internal const val WHITE_COLOR = "@android:color/white"
internal const val THEME_COLOR_OPTION_DESCRIPTION = "Can be a hex color (#AARRGGBB or #RRGGBB) or a color resource reference."

internal val DARK_THEME_COLOR_VALUES = mapOf(
    "Pure black" to PURE_BLACK_COLOR,
    "Material You" to "@android:color/system_neutral1_900",
    "Classic (old YouTube)" to "#FF212121",
    "Catppuccin (Mocha)" to "#FF181825",
    "Dark pink" to "#FF290025",
    "Dark blue" to "#FF001029",
    "Dark green" to "#FF002905",
    "Dark yellow" to "#FF282900",
    "Dark orange" to "#FF291800",
    "Dark red" to "#FF290000",
)

internal val LIGHT_THEME_COLOR_VALUES = mapOf(
    "White" to WHITE_COLOR,
    "Material You" to "@android:color/system_neutral1_50",
    "Catppuccin (Latte)" to "#FFE6E9EF",
    "Light pink" to "#FFFCCFF3",
    "Light blue" to "#FFD1E0FF",
    "Light green" to "#FFCCFFCC",
    "Light yellow" to "#FFFDFFCC",
    "Light orange" to "#FFFFE6CC",
    "Light red" to "#FFFFD6D6",
)

internal val THEME_DEFAULT_DARK_COLOR_NAMES = setOf(
    "yt_black0", "yt_black1", "yt_black1_opacity95", "yt_black1_opacity98",
    "yt_black2", "yt_black3", "yt_black4", "yt_status_bar_background_dark",
    "material_grey_850"
)

internal val THEME_DEFAULT_LIGHT_COLOR_NAMES = setOf(
    "yt_white1", "yt_white1_opacity95", "yt_white1_opacity98",
    "yt_white2", "yt_white3", "yt_white4"
)

/**
 * @param colorString #AARRGGBB #RRGGBB, or an Android color resource name.
 */
internal fun validateColorName(colorString: String): Boolean {
    if (colorString.startsWith("#")) {
        // Check for #RRGGBB (7 characters) or #AARRGGBB (9 characters)
        val hex = colorString.substring(1)
        if (hex.length == 6 || hex.length == 8) {
            return hex.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }
        }
        return false
    }

    if (colorString.startsWith("@android:color/")) {
        // Cannot easily validate Android built-in colors, so assume it's a correct color.
        return true
    }

    val colorNamePrefix = "@color/"
    val name = if (colorString.startsWith(colorNamePrefix)) {
        colorString.substring(colorNamePrefix.length)
    } else {
        colorNamePrefix
    }

    return resourceMappings.find { it.type == "color" && it.name == name } != null
}

internal fun baseThemePatch(
    extensionClassDescriptor: String,
    block: BytecodePatchBuilder.() -> Unit = {},
    executeBlock: BytecodePatchContext.() -> Unit = {}
) = bytecodePatch(
    name = "Theme",
    description = "Adds options for theming and applies a custom background theme " +
            "(dark background theme defaults to pure black).",
) {
    block()

    dependsOn(lithoColorHookPatch)

    execute {
        executeBlock()

        lithoColorOverrideHook(extensionClassDescriptor, "getValue")
    }
}

internal fun baseThemeResourcePatch(
    darkColorNames: Set<String> = THEME_DEFAULT_DARK_COLOR_NAMES,
    darkColorReplacement: () -> String,
    lightColorNames: Set<String> = THEME_DEFAULT_LIGHT_COLOR_NAMES,
    lightColorReplacement: (() -> String)? = null
) = resourcePatch {

    dependsOn(resourceMappingPatch)

    execute {
        fun getLogger() = Logger.getLogger(this::class.java.name)

        // Cannot use an option validator, because resources
        // have not been decoded when validator is called.
        val darkColor = darkColorReplacement()
        if (!validateColorName(darkColor)) {
            return@execute getLogger().severe(
                "Invalid dark theme color: $darkColor"
            )
        }

        val lightColor = lightColorReplacement?.invoke()
        if (lightColor != null && !validateColorName(lightColor)) {
            return@execute getLogger().severe(
                "Invalid light theme color: $lightColor"
            )
        }

        document("res/values/colors.xml").use { document ->
            val resourcesNode = document.getElementsByTagName("resources").item(0)

            resourcesNode.childElementsSequence().forEach { node ->
                val name = node.getAttribute("name")
                when {
                    name in darkColorNames -> node.textContent = darkColor
                    lightColor != null && name in lightColorNames -> node.textContent = lightColor
                }
            }
        }
    }
}
