package app.revanced.patches.music.layout.theme

import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.shared.layout.theme.THEME_DEFAULT_DARK_COLOR_NAMES
import app.revanced.patches.shared.layout.theme.baseThemePatch
import app.revanced.patches.shared.layout.theme.baseThemeResourcePatch
import app.revanced.patches.shared.layout.theme.darkThemeBackgroundColorOption
import app.revanced.patches.shared.misc.settings.overrideThemeColors

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/music/patches/theme/ThemePatch;"

@Suppress("unused")
val themePatch = baseThemePatch(
    extensionClassDescriptor = EXTENSION_CLASS_DESCRIPTOR,

    block = {
        dependsOn(
            sharedExtensionPatch,
            baseThemeResourcePatch(
                darkColorNames = THEME_DEFAULT_DARK_COLOR_NAMES + setOf(
                    "yt_black_pure",
                    "yt_black_pure_opacity80",
                    "ytm_color_grey_12",
                    "material_grey_800"
                )
            )
        )

        compatibleWith(
            "com.google.android.apps.youtube.music"(
                "7.29.52",
                "8.10.52"
            )
        )
    },

    executeBlock = {
        overrideThemeColors(
            null,
            darkThemeBackgroundColorOption.value!!
        )
    }
)
