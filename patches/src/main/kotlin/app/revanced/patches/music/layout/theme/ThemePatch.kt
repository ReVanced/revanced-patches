package app.revanced.patches.music.layout.theme

import app.revanced.patcher.patch.stringOption
import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.shared.layout.theme.DARK_THEME_COLOR_VALUES
import app.revanced.patches.shared.layout.theme.PURE_BLACK_COLOR
import app.revanced.patches.shared.layout.theme.THEME_COLOR_OPTION_DESCRIPTION
import app.revanced.patches.shared.layout.theme.THEME_DEFAULT_DARK_COLOR_NAMES
import app.revanced.patches.shared.layout.theme.baseThemePatch
import app.revanced.patches.shared.layout.theme.baseThemeResourcePatch

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/music/patches/theme/ThemePatch;"

@Suppress("unused")
val themePatch = baseThemePatch(
    extensionClassDescriptor = EXTENSION_CLASS_DESCRIPTOR,

    block = {
        val darkThemeBackgroundColor by stringOption(
            key = "darkThemeBackgroundColor",
            default = PURE_BLACK_COLOR,
            values = DARK_THEME_COLOR_VALUES,
            title = "Dark theme background color",
            description = THEME_COLOR_OPTION_DESCRIPTION
        )

        dependsOn(
            sharedExtensionPatch,
            baseThemeResourcePatch(
                darkColorNames = THEME_DEFAULT_DARK_COLOR_NAMES + setOf(
                    "yt_black_pure",
                    "yt_black_pure_opacity80",
                    "ytm_color_grey_12",
                    "material_grey_800"
                ),
                darkColorReplacement = { darkThemeBackgroundColor!! }
            )
        )

        compatibleWith(
            "com.google.android.apps.youtube.music"(
                "7.29.52",
                "8.10.52"
            )
        )
    }
)
