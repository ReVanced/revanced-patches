package app.revanced.patches.youtube.layout.theme

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
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
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.forEachChildElement
import app.revanced.util.indexOfFirstWideLiteralInstructionValueOrThrow
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import org.w3c.dom.Element

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/theme/ThemePatch;"

internal const val GRADIENT_LOADING_SCREEN_AB_CONSTANT = 45412406L

@Suppress("unused")
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
        resourcePatch {
            dependsOn(
                settingsPatch,
                resourceMappingPatch,
                addResourcesPatch,
            )

            execute { context ->
                addResources("youtube", "layout.theme.themeResourcePatch")

                PreferenceScreen.SEEKBAR.addPreferences(
                    SwitchPreference("revanced_seekbar_custom_color"),
                    TextPreference("revanced_seekbar_custom_color_value", inputType = InputType.TEXT_CAP_CHARACTERS),
                )

                // Edit theme colors via resources.
                context.document["res/values/colors.xml"].use { document ->

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
                    context.document[resourceFile].use { document ->

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
                        context.document[resourceFile].use { document ->
                            document.getElementsByTagName("layer-list").item(0).forEachChildElement { node ->
                                if (node.hasAttribute("android:drawable")) {
                                    node.setAttribute("android:drawable", "@color/$splashBackgroundColor")
                                    return@editSplashScreen
                                }
                            }

                            throw PatchException("Failed to modify launch screen")
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
            "18.37.36",
            "18.38.44",
            "18.43.45",
            "18.44.41",
            "18.45.43",
            "18.48.39",
            "18.49.37",
            "19.01.34",
            "19.02.39",
            "19.03.36",
            "19.04.38",
            "19.05.36",
            "19.06.39",
            "19.07.40",
            "19.08.36",
            "19.09.38",
            "19.10.39",
            "19.11.43",
            "19.12.41",
            "19.13.37",
            "19.14.43",
            "19.15.36",
            "19.16.39",
        ),
    )

    val useGradientLoadingScreenMatch by useGradientLoadingScreenFingerprint()
    val themeHelperLightColorMatch by themeHelperLightColorFingerprint()
    val themeHelperDarkColorMatch by themeHelperDarkColorFingerprint()

    execute {
        addResources("youtube", "layout.theme.themePatch")

        PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            SwitchPreference("revanced_gradient_loading_screen"),
        )

        useGradientLoadingScreenMatch.mutableMethod.apply {

            val isEnabledIndex = indexOfFirstWideLiteralInstructionValueOrThrow(GRADIENT_LOADING_SCREEN_AB_CONSTANT) + 3
            val isEnabledRegister = getInstruction<OneRegisterInstruction>(isEnabledIndex - 1).registerA

            addInstructions(
                isEnabledIndex,
                """
                    invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->gradientLoadingScreenEnabled()Z
                    move-result v$isEnabledRegister
                """,
            )
        }
        mapOf(
            themeHelperLightColorMatch to lightThemeBackgroundColor,
            themeHelperDarkColorMatch to darkThemeBackgroundColor,
        ).forEach { (match, color) ->
            match.mutableMethod.apply {
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
