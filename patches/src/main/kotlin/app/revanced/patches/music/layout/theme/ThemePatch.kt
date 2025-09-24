package app.revanced.patches.music.layout.theme

import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.shared.layout.theme.DARK_THEME_COLOR_VALUES
import app.revanced.patches.shared.layout.theme.PURE_BLACK_COLOR
import app.revanced.patches.shared.layout.theme.baseThemePatch
import app.revanced.patches.shared.layout.theme.editThemeColors
import app.revanced.patches.shared.layout.theme.lithoColorHookPatch
import app.revanced.patches.shared.layout.theme.lithoColorOverrideHook
import app.revanced.util.childElementsSequence
import org.w3c.dom.Element

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/music/patches/theme/ThemePatch;"

@Suppress("unused")
val musicThemeBytecodePatch = baseThemePatch(
    block = {
        val darkThemeBackgroundColor by stringOption(
            key = "darkThemeBackgroundColor",
            default = PURE_BLACK_COLOR,
            values = DARK_THEME_COLOR_VALUES,
            title = "Dark theme background color",
            description = "Can be a hex color (#AARRGGBB) or a color resource reference.",
        )

        dependsOn(
            sharedExtensionPatch,
            addResourcesPatch,
            lithoColorHookPatch,
            resourcePatch {
                execute {
                    // Edit theme colors via resources.
                    document("res/values/colors.xml").use { document ->
                        editThemeColors(document, darkThemeBackgroundColor!!)

                        // Music-specific dark colors.
                        val resourcesNode = document.getElementsByTagName("resources").item(0) as Element

                        resourcesNode.childElementsSequence().forEach { node ->
                            when (node.getAttribute("name")) {
                                "yt_black_pure",
                                "yt_black_pure_opacity80",
                                "ytm_color_grey_12",
                                "material_grey_800" -> node.textContent = darkThemeBackgroundColor
                            }
                        }
                    }
                }
            }
        )

        compatibleWith(
            "com.google.android.apps.youtube.music"(
                "7.29.52",
                "8.10.52"
            )
        )
    },
    executeBlock = {
        lithoColorOverrideHook(EXTENSION_CLASS_DESCRIPTOR, "getValue")
    }
)
