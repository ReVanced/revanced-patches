package app.revanced.patches.youtube.layout.theme

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.InputType
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.shared.misc.settings.preference.TextPreference
import app.revanced.patches.youtube.layout.seekbar.seekbarColorPatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.is_19_25_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.forEachChildElement
import app.revanced.util.insertFeatureFlagBooleanOverride
import org.w3c.dom.Element

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/theme/ThemePatch;"

internal const val GRADIENT_LOADING_SCREEN_AB_CONSTANT = 45412406L

val themePatch = bytecodePatch(
    name = "Theme",
    description = "Adds options for theming and applies a custom background theme (dark background theme defaults to amoled black).",
) {
    val amoledBlackColor = "@android:color/black"
    val whiteColor = "@android:color/white"

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

    val lightThemeBackgroundColor by stringOption(
        key = "lightThemeBackgroundColor",
        default = whiteColor,
        values = mapOf(
            "White" to whiteColor,
            "Material You" to "@android:color/system_neutral1_50",
            "Catppuccin (Latte)" to "#FFE6E9EF",
            "Light pink" to "#FFFCCFF3",
            "Light blue" to "#FFD1E0FF",
            "Light green" to "#FFCCFFCC",
            "Light yellow" to "#FFFDFFCC",
            "Light orange" to "#FFFFE6CC",
            "Light red" to "#FFFFD6D6",
        ),
        title = "Light theme background color",
        description = "Can be a hex color (#AARRGGBB) or a color resource reference.",
    )

    dependsOn(
        lithoColorHookPatch,
        seekbarColorPatch,
        versionCheckPatch,
        resourcePatch {
            dependsOn(
                settingsPatch,
                resourceMappingPatch,
                addResourcesPatch,
            )

            execute {
                addResources("youtube", "layout.theme.themeResourcePatch")

                PreferenceScreen.SEEKBAR.addPreferences(
                    SwitchPreference("revanced_seekbar_custom_color"),
                    TextPreference("revanced_seekbar_custom_color_primary", inputType = InputType.TEXT_CAP_CHARACTERS),
                )

                if (is_19_25_or_greater) {
                    PreferenceScreen.SEEKBAR.addPreferences(
                        TextPreference("revanced_seekbar_custom_color_accent", inputType = InputType.TEXT_CAP_CHARACTERS),
                    )
                }

                // Edit theme colors via resources.
                document("res/values/colors.xml").use { document ->

                    val resourcesNode = document.getElementsByTagName("resources").item(0) as Element

                    val children = resourcesNode.childNodes
                    for (i in 0 until children.length) {
                        val node = children.item(i) as? Element ?: continue

                        node.textContent =
                            when (node.getAttribute("name")) {
                                "yt_black0", "yt_black1", "yt_black1_opacity95", "yt_black1_opacity98", "yt_black2", "yt_black3",
                                "yt_black4", "yt_status_bar_background_dark", "material_grey_850",
                                -> darkThemeBackgroundColor ?: continue

                                "yt_white1", "yt_white1_opacity95", "yt_white1_opacity98",
                                "yt_white2", "yt_white3", "yt_white4",
                                -> lightThemeBackgroundColor ?: continue

                                else -> continue
                            }
                    }
                }

                fun addColorResource(
                    resourceFile: String,
                    colorName: String,
                    colorValue: String,
                ) {
                    document(resourceFile).use { document ->

                        val resourcesNode = document.getElementsByTagName("resources").item(0) as Element

                        resourcesNode.appendChild(
                            document.createElement("color").apply {
                                setAttribute("name", colorName)
                                setAttribute("category", "color")
                                textContent = colorValue
                            },
                        )
                    }
                }

                val splashBackgroundColor = "revanced_splash_background_color"

                // Add a dynamic background color to the colors.xml file.
                lightThemeBackgroundColor?.let {
                    addColorResource("res/values/colors.xml", splashBackgroundColor, it)
                }

                darkThemeBackgroundColor?.let {
                    addColorResource("res/values-night/colors.xml", splashBackgroundColor, it)
                }

                // Edit splash screen files and change the background color,
                // if the background colors are set.
                if (darkThemeBackgroundColor != null && lightThemeBackgroundColor != null) {
                    val splashScreenResourceFiles =
                        listOf(
                            "res/drawable/quantum_launchscreen_youtube.xml",
                            "res/drawable-sw600dp/quantum_launchscreen_youtube.xml",
                        )

                    splashScreenResourceFiles.forEach editSplashScreen@{ resourceFile ->
                        document(resourceFile).use { document ->
                            document.getElementsByTagName("layer-list").item(0).forEachChildElement { node ->
                                if (node.hasAttribute("android:drawable")) {
                                    node.setAttribute("android:drawable", "@color/$splashBackgroundColor")
                                    return@editSplashScreen
                                }
                            }

                            throw PatchException("Failed to modify launch screen")
                        }
                    }

                    // Fix the splash screen dark mode background color.
                    // In 19.32+ the dark mode splash screen is white and fades to black.
                    // Maybe it's a bug in YT, or maybe it intentionally. Who knows.
                    document("res/values-night/styles.xml").use { document ->
                        val resourcesNode = document.getElementsByTagName("resources").item(0) as Element
                        val childNodes = resourcesNode.childNodes

                        for (i in 0 until childNodes.length) {
                            val node = childNodes.item(i) as? Element ?: continue
                            val nodeAttributeName = node.getAttribute("name")
                            if (nodeAttributeName.startsWith("Theme.YouTube.Launcher")) {
                                val nodeAttributeParent = node.getAttribute("parent")

                                val style = document.createElement("style")
                                style.setAttribute("name", "Theme.YouTube.Home")
                                style.setAttribute("parent", nodeAttributeParent)

                                val windowItem = document.createElement("item")
                                windowItem.setAttribute("name", "android:windowBackground")
                                windowItem.textContent = "@color/$splashBackgroundColor"
                                style.appendChild(windowItem)

                                resourcesNode.removeChild(node)
                                resourcesNode.appendChild(style)
                            }
                        }
                    }
                }
            }
        },
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "18.38.44",
            "18.49.37",
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
            "19.45.38",
            "19.46.42",
            "19.47.53",
        ),
    )

    execute {
        addResources("youtube", "layout.theme.themePatch")

        PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            SwitchPreference("revanced_gradient_loading_screen"),
        )

        useGradientLoadingScreenFingerprint.method.insertFeatureFlagBooleanOverride(
            GRADIENT_LOADING_SCREEN_AB_CONSTANT,
            "$EXTENSION_CLASS_DESCRIPTOR->gradientLoadingScreenEnabled(Z)Z"
        )

        mapOf(
            themeHelperLightColorFingerprint to lightThemeBackgroundColor,
            themeHelperDarkColorFingerprint to darkThemeBackgroundColor,
        ).forEach { (fingerprint, color) ->
            fingerprint.method.apply {
                addInstructions(
                    0,
                    """
                        const-string v0, "$color"
                        return-object v0
                    """,
                )
            }
        }

        lithoColorOverrideHook(EXTENSION_CLASS_DESCRIPTOR, "getValue")
    }
}
