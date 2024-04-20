package app.revanced.patches.youtube.layout.theme

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.stringPatchOption
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.layout.seekbar.SeekbarColorBytecodePatch
import app.revanced.patches.youtube.layout.theme.fingerprints.ThemeHelperDarkColorFingerprint
import app.revanced.patches.youtube.layout.theme.fingerprints.ThemeHelperLightColorFingerprint
import app.revanced.patches.youtube.layout.theme.fingerprints.UseGradientLoadingScreenFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.exception
import app.revanced.util.indexOfFirstWideLiteralInstructionValue
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Theme",
    description = "Adds options for theming and applies a custom background theme (dark background theme defaults to amoled black).",
    dependencies = [
        LithoColorHookPatch::class,
        SeekbarColorBytecodePatch::class,
        ThemeResourcePatch::class,
        IntegrationsPatch::class,
        SettingsPatch::class,
        AddResourcesPatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
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
                "19.11.43"
            ]
        )
    ]
)
@Suppress("unused")
object ThemeBytecodePatch : BytecodePatch(
    setOf(
        UseGradientLoadingScreenFingerprint,
        ThemeHelperLightColorFingerprint,
        ThemeHelperDarkColorFingerprint
    )
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/theme/ThemePatch;"

    internal const val GRADIENT_LOADING_SCREEN_AB_CONSTANT = 45412406L

    private const val AMOLED_BLACK_COLOR = "@android:color/black"
    private const val WHITE_COLOR = "@android:color/white"

    internal val darkThemeBackgroundColor by stringPatchOption(
        key = "darkThemeBackgroundColor",
        default = AMOLED_BLACK_COLOR,
        values = mapOf(
            "Amoled black" to AMOLED_BLACK_COLOR,
            "Material You" to "@android:color/system_neutral1_900",
            "Classic (old YouTube)" to "#FF212121",
            "Catppuccin (Mocha)" to "#FF181825",
            "Dark pink" to "#FF290025",
            "Dark blue" to "#FF001029",
            "Dark green" to "#FF002905",
            "Dark yellow" to "#FF282900",
            "Dark orange" to "#FF291800",
            "Dark red" to "#FF290000"
        ),
        title = "Dark theme background color",
        description = "Can be a hex color (#AARRGGBB) or a color resource reference.",
    )

    internal val lightThemeBackgroundColor by stringPatchOption(
        key = "lightThemeBackgroundColor",
        default = WHITE_COLOR,
        values = mapOf(
            "White" to WHITE_COLOR,
            "Material You" to "@android:color/system_neutral1_50",
            "Catppuccin (Latte)" to "#FFE6E9EF",
            "Light pink" to "#FFFCCFF3",
            "Light blue" to "#FFD1E0FF",
            "Light green" to "#FFCCFFCC",
            "Light yellow" to "#FFFDFFCC",
            "Light orange" to "#FFFFE6CC",
            "Light red" to "#FFFFD6D6"
        ),
        title = "Light theme background color",
        description = "Can be a hex color (#AARRGGBB) or a color resource reference.",
    )

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            SwitchPreference("revanced_gradient_loading_screen")
        )

        UseGradientLoadingScreenFingerprint.result?.mutableMethod?.apply {
            val isEnabledIndex = indexOfFirstWideLiteralInstructionValue(GRADIENT_LOADING_SCREEN_AB_CONSTANT) + 3
            val isEnabledRegister = getInstruction<OneRegisterInstruction>(isEnabledIndex - 1).registerA

            addInstructions(
                isEnabledIndex,
                """
                    invoke-static { }, $INTEGRATIONS_CLASS_DESCRIPTOR->gradientLoadingScreenEnabled()Z
                    move-result v$isEnabledRegister
                """
            )
        } ?: throw UseGradientLoadingScreenFingerprint.exception


        mapOf(
            ThemeHelperLightColorFingerprint to lightThemeBackgroundColor,
            ThemeHelperDarkColorFingerprint to darkThemeBackgroundColor
        ).forEach { (fingerprint, color) ->
            fingerprint.resultOrThrow().mutableMethod.apply {
                addInstructions(
                    0, """
                        const-string v0, "$color"
                        return-object v0
                    """
                )
            }
        }

        LithoColorHookPatch.lithoColorOverrideHook(INTEGRATIONS_CLASS_DESCRIPTOR, "getValue")
    }
}
