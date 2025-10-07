package app.revanced.patches.shared.layout.theme

import app.revanced.patcher.patch.BytecodePatchBuilder
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.util.childElementsSequence
import java.util.Locale

internal const val THEME_COLOR_OPTION_DESCRIPTION = "Can be a hex color (#RRGGBB) or a color resource reference."

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
        // #RRGGBB or #AARRGGBB
        val hex = colorString.substring(1).uppercase(Locale.US)

        if (hex.length == 8) {
            // Transparent colors will crash the app.
            if (hex[0] != 'F' || hex[1] != 'F') {
                return false
            }
        } else if (hex.length != 6) {
            return false
        }

        return hex.all { it.isDigit() || it in 'A'..'F' }
    }

    if (colorString.startsWith("@android:color/")) {
        // Cannot easily validate Android built-in colors, so assume it's a correct color.
        return true
    }

    // Allow any color name, because if it's invalid it will
    // throw an exception during resource compilation.
    return colorString.startsWith("@color/")
}

/**
 * Dark theme color option for YouTube and YT Music Theme patches.
 */
internal val darkThemeBackgroundColorOption = stringOption(
    key = "darkThemeBackgroundColor",
    default = "@android:color/black",
    values = mapOf(
        "Pure black" to "@android:color/black",
        "Material You" to "@android:color/system_neutral1_900",
        "Classic (old YouTube)" to "#212121",
        "Catppuccin (Mocha)" to "#181825",
        "Dark pink" to "#290025",
        "Dark blue" to "#001029",
        "Dark green" to "#002905",
        "Dark yellow" to "#282900",
        "Dark orange" to "#291800",
        "Dark red" to "#290000",
    ),
    title = "Dark theme background color",
    description = THEME_COLOR_OPTION_DESCRIPTION
)

/**
 * Shared theme patch for YouTube and YT Music.
 */
internal fun baseThemePatch(
    extensionClassDescriptor: String,
    block: BytecodePatchBuilder.() -> Unit,
    executeBlock: BytecodePatchContext.() -> Unit = {}
) = bytecodePatch(
    name = "Theme",
    description = "Adds options for theming and applies a custom background theme " +
            "(dark background theme defaults to pure black).",
) {
    darkThemeBackgroundColorOption()

    block()

    dependsOn(lithoColorHookPatch)

    execute {
        executeBlock()

        lithoColorOverrideHook(extensionClassDescriptor, "getValue")
    }
}

internal fun baseThemeResourcePatch(
    darkColorNames: Set<String> = THEME_DEFAULT_DARK_COLOR_NAMES,
    lightColorNames: Set<String> = THEME_DEFAULT_LIGHT_COLOR_NAMES,
    lightColorReplacement: (() -> String)? = null
) = resourcePatch {

    execute {
        // After patch option validators are fixed https://github.com/ReVanced/revanced-patcher/issues/372
        // This should changed to a patch option validator.
        val darkColor by darkThemeBackgroundColorOption
        if (!validateColorName(darkColor!!)) {
            throw PatchException("Invalid dark theme color: $darkColor")
        }

        val lightColor = lightColorReplacement?.invoke()
        if (lightColor != null && !validateColorName(lightColor)) {
            throw PatchException("Invalid light theme color: $lightColor")
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
