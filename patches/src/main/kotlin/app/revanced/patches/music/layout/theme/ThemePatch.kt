package app.revanced.patches.music.layout.theme

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.youtube.layout.theme.lithoColorHookPatch
import app.revanced.patches.youtube.layout.theme.lithoColorOverrideHook
import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.music.misc.settings.settingsPatch
import app.revanced.util.childElementsSequence
import org.w3c.dom.Element

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/music/patches/theme/ThemePatch;"

private val musicThemeBytecodePatch = bytecodePatch(
    name = "Theme",
    description = "Adds options for theming and applies a custom background theme " +
            "(dark background theme defaults to amoled black).",
) {
    val amoledBlackColor = "@android:color/black"

    val darkThemeBackgroundColor by stringOption(
        key = "darkThemeBackgroundColor",
        default = amoledBlackColor,
        values = mapOf(
            "Amoled black" to amoledBlackColor,
            "Material You" to "@android:color/system_neutral1_900",
            "Classic (old YouTube)" to "#FF212121",
            "Catppuccin (Mocha)" to "#FF181825",
            "Dark pink" to "#FF290025",
            "Dark blue" to "#FF001029",
            "Dark green" to "#FF002905",
            "Dark yellow" to "#FF282900",
            "Dark orange" to "#FF291800",
            "Dark red" to "#FF290000",
        ),
        title = "Dark theme background color",
        description = "Can be a hex color (#AARRGGBB) or a color resource reference.",
    )

    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
        lithoColorHookPatch,
        resourcePatch {
            dependsOn(
                settingsPatch,
                resourceMappingPatch,
            )
            execute {
                // Edit theme colors via resources.
                document("res/values/colors.xml").use { document ->
                    val resourcesNode = document.getElementsByTagName("resources").item(0) as Element

                    resourcesNode.childElementsSequence().forEach { node ->
                        when (node.getAttribute("name")) {
                            "yt_black0", "yt_black1", "yt_black1_opacity95", "yt_black1_opacity98",
                            "yt_black2", "yt_black3", "yt_black4", "yt_black_pure",
                            "yt_black_pure_opacity80", "yt_status_bar_background_dark",
                            "ytm_color_grey_12", "material_grey_800", "material_grey_850",
                                -> node.textContent = darkThemeBackgroundColor
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

    execute {
        lithoColorOverrideHook(EXTENSION_CLASS_DESCRIPTOR, "getValue")
    }
}
