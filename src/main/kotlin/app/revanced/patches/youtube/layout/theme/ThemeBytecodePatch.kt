package app.revanced.patches.youtube.layout.theme

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.layout.seekbar.seekbarColorBytecodePatch
import app.revanced.patches.youtube.layout.theme.fingerprints.themeHelperDarkColorFingerprint
import app.revanced.patches.youtube.layout.theme.fingerprints.themeHelperLightColorFingerprint
import app.revanced.patches.youtube.layout.theme.fingerprints.useGradientLoadingScreenFingerprint
import app.revanced.patches.youtube.misc.integrations.integrationsPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.indexOfFirstWideLiteralInstructionValue
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val INTEGRATIONS_CLASS_DESCRIPTOR =
    "Lapp/revanced/integrations/youtube/patches/theme/ThemePatch;"

@Suppress("unused")
val themePatch = bytecodePatch(
    name = "Theme",
    description = "Adds options for theming and applies a custom background theme (dark background theme defaults to amoled black).",
) {
    dependsOn(
        lithoColorHookPatch,
        seekbarColorBytecodePatch,
        themeResourcePatch,
        integrationsPatch,
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
        ),
    )

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

    val useGradientLoadingScreenResult by useGradientLoadingScreenFingerprint
    val themeHelperLightColorResult by themeHelperLightColorFingerprint
    val themeHelperDarkColorResult by themeHelperDarkColorFingerprint

    execute {
        addResources("youtube", "layout.theme.ThemeResourcePatch")

        PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            SwitchPreference("revanced_gradient_loading_screen"),
        )

        useGradientLoadingScreenResult.mutableMethod.apply {
            val gradientLoadingScreenABConstant = 45412406L

            val isEnabledIndex = indexOfFirstWideLiteralInstructionValue(gradientLoadingScreenABConstant) + 3
            val isEnabledRegister = getInstruction<OneRegisterInstruction>(isEnabledIndex - 1).registerA

            addInstructions(
                isEnabledIndex,
                """
                    invoke-static { }, $INTEGRATIONS_CLASS_DESCRIPTOR->gradientLoadingScreenEnabled()Z
                    move-result v$isEnabledRegister
                """,
            )
        }
        mapOf(
            themeHelperLightColorResult to lightThemeBackgroundColor,
            themeHelperDarkColorResult to darkThemeBackgroundColor,
        ).forEach { (result, color) ->
            result.mutableMethod.apply {
                addInstructions(
                    0,
                    """
                        const-string v0, "$color"
                        return-object v0
                    """,
                )
            }
        }

        lithoColorOverrideHook(INTEGRATIONS_CLASS_DESCRIPTOR, "getValue")
    }
}
