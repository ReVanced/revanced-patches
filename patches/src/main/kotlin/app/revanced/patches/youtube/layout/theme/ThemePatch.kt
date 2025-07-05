package app.revanced.patches.youtube.layout.theme

import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.settings.overrideThemeColors
import app.revanced.patches.shared.misc.settings.preference.BasePreference
import app.revanced.patches.shared.misc.settings.preference.InputType
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.patches.shared.misc.settings.preference.PreferenceCategory
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference.Sorting
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.shared.misc.settings.preference.TextPreference
import app.revanced.patches.youtube.layout.seekbar.seekbarColorPatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.is_19_25_or_greater
import app.revanced.patches.youtube.misc.playservice.is_19_47_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.childElementsSequence
import app.revanced.util.forEachChildElement
import app.revanced.util.insertLiteralOverride
import org.w3c.dom.Element

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/theme/ThemePatch;"

val themePatch = bytecodePatch(
    name = "Theme",
    description = "Adds options for theming and applies a custom background theme " +
            "(dark background theme defaults to amoled black).",
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
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
        lithoColorHookPatch,
        seekbarColorPatch,
        versionCheckPatch,
        resourcePatch {
            dependsOn(
                settingsPatch,
                resourceMappingPatch,
            )

            execute {
                val preferences = mutableSetOf<BasePreference>(
                    SwitchPreference("revanced_seekbar_custom_color"),
                    TextPreference("revanced_seekbar_custom_color_primary",
                        tag = "app.revanced.extension.shared.settings.preference.ColorPickerPreference",
                        inputType = InputType.TEXT_CAP_CHARACTERS),
                )

                if (is_19_25_or_greater) {
                    preferences += TextPreference("revanced_seekbar_custom_color_accent",
                        tag = "app.revanced.extension.shared.settings.preference.ColorPickerPreference",
                        inputType = InputType.TEXT_CAP_CHARACTERS)
                }

                PreferenceScreen.SEEKBAR.addPreferences(
                    PreferenceCategory(
                        titleKey = null,
                        sorting = Sorting.UNSORTED,
                        tag = "app.revanced.extension.shared.settings.preference.NoTitlePreferenceCategory",
                        preferences = preferences
                    )
                )

                overrideThemeColors(lightThemeBackgroundColor!!, darkThemeBackgroundColor!!)

                // Edit theme colors via resources.
                document("res/values/colors.xml").use { document ->
                    val resourcesNode = document.getElementsByTagName("resources").item(0) as Element

                    resourcesNode.childElementsSequence().forEach { node ->
                        when (node.getAttribute("name")) {
                            "yt_black0", "yt_black1", "yt_black1_opacity95", "yt_black1_opacity98",
                            "yt_black2", "yt_black3", "yt_black4", "yt_status_bar_background_dark",
                            "material_grey_850",
                                -> node.textContent = darkThemeBackgroundColor

                            "yt_white1", "yt_white1_opacity95", "yt_white1_opacity98",
                            "yt_white2", "yt_white3", "yt_white4",
                                -> node.textContent = lightThemeBackgroundColor
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
                            }
                        )
                    }
                }

                // Add a dynamic background color to the colors.xml file.
                val splashBackgroundColorKey = "revanced_splash_background_color"
                addColorResource("res/values/colors.xml", splashBackgroundColorKey, lightThemeBackgroundColor!!)
                addColorResource("res/values-night/colors.xml", splashBackgroundColorKey, darkThemeBackgroundColor!!)

                // Edit splash screen files and change the background color,
                arrayOf(
                    "res/drawable/quantum_launchscreen_youtube.xml",
                    "res/drawable-sw600dp/quantum_launchscreen_youtube.xml",
                ).forEach editSplashScreen@{ resourceFileName ->
                    document(resourceFileName).use { document ->
                        document.getElementsByTagName("layer-list").item(0).forEachChildElement { node ->
                            if (node.hasAttribute("android:drawable")) {
                                node.setAttribute("android:drawable", "@color/$splashBackgroundColorKey")
                                return@editSplashScreen
                            }
                        }

                        throw PatchException("Failed to modify launch screen")
                    }
                }

                // Fix the splash screen dark mode background color.
                // In 19.32+ the dark mode splash screen is white and fades to black.
                // Maybe it's a bug in YT, or maybe it intentionally. Who knows.
                document("res/values-night-v27/styles.xml").use { document ->
                    // Create a night mode specific override for the splash screen background.
                    val style = document.createElement("style")
                    style.setAttribute("name", "Theme.YouTube.Home")
                    style.setAttribute("parent", "@style/Base.V27.Theme.YouTube.Home")

                    // Fix status and navigation bar showing white on some Android devices,
                    // such as SDK 28 Android 10 medium tablet.
                    val colorSplashBackgroundColor = "@color/$splashBackgroundColorKey"
                    arrayOf(
                        "android:navigationBarColor" to colorSplashBackgroundColor,
                        "android:windowBackground" to colorSplashBackgroundColor,
                        "android:colorBackground" to colorSplashBackgroundColor,
                        "colorPrimaryDark" to colorSplashBackgroundColor,
                        "android:windowLightStatusBar" to "false",
                    ).forEach { (name, value) ->
                        val styleItem = document.createElement("item")
                        styleItem.setAttribute("name", name)
                        styleItem.textContent = value
                        style.appendChild(styleItem)
                    }

                    val resourcesNode = document.getElementsByTagName("resources").item(0) as Element
                    resourcesNode.appendChild(style)
                }
            }
        }
    )

    compatibleWith(
        "com.google.android.youtube"(
            "19.34.42",
            "19.43.41",
            "19.47.53",
            "20.07.39",
            "20.12.46",
            "20.13.41",
        )
    )

    execute {
        addResources("youtube", "layout.theme.themePatch")

        PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            SwitchPreference("revanced_gradient_loading_screen")
        )

        if (is_19_47_or_greater) {
            PreferenceScreen.GENERAL_LAYOUT.addPreferences(
                ListPreference("revanced_splash_screen_animation_style")
            )
        }

        useGradientLoadingScreenFingerprint.method.insertLiteralOverride(
            GRADIENT_LOADING_SCREEN_AB_CONSTANT,
            "$EXTENSION_CLASS_DESCRIPTOR->gradientLoadingScreenEnabled(Z)Z"
        )

        if (is_19_47_or_greater) {
            // Lottie splash screen exists in earlier versions, but it may not be always on.
            splashScreenStyleFingerprint.method.insertLiteralOverride(
                SPLASH_SCREEN_STYLE_FEATURE_FLAG,
                "$EXTENSION_CLASS_DESCRIPTOR->getLoadingScreenType(I)I"
            )
        }

        lithoColorOverrideHook(EXTENSION_CLASS_DESCRIPTOR, "getValue")
    }
}
