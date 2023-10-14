package app.revanced.patches.youtube.layout.theme

import app.revanced.extensions.exception
import app.revanced.extensions.indexOfFirstWideLiteralInstructionValue
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.types.StringPatchOption.Companion.stringPatchOption
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.layout.seekbar.SeekbarColorBytecodePatch
import app.revanced.patches.youtube.layout.theme.fingerprints.UseGradientLoadingScreenFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Theme",
    description = "Applies a custom theme.",
    dependencies = [
        LithoColorHookPatch::class,
        SeekbarColorBytecodePatch::class,
        ThemeResourcePatch::class,
        IntegrationsPatch::class,
        SettingsPatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.37.36",
                "18.38.44"
            ]
        )
    ]
)
@Suppress("unused")
object ThemeBytecodePatch : BytecodePatch(
    setOf(UseGradientLoadingScreenFingerprint)
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/patches/theme/ThemePatch;"

    internal const val GRADIENT_LOADING_SCREEN_AB_CONSTANT = 45412406L

    internal val darkThemeBackgroundColor by stringPatchOption(
        key = "darkThemeBackgroundColor",
        default = "@android:color/black",
        title = "Dark theme background color",
        description = "The background color of the dark theme. Can be a hex color or a resource reference.",
    )

    internal val lightThemeBackgroundColor by stringPatchOption(
        key = "lightThemeBackgroundColor",
        default = "@android:color/white",
        title = "Light theme background color",
        description = "The background color of the light theme. Can be a hex color or a resource reference.",
    )

    override fun execute(context: BytecodeContext) {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_gradient_loading_screen",
                StringResource("revanced_gradient_loading_screen_title", "Enable gradient loading screen"),
                StringResource(
                    "revanced_gradient_loading_screen_summary_on",
                    "Loading screen will have a gradient background"
                ),
                StringResource(
                    "revanced_gradient_loading_screen_summary_off",
                    "Loading screen will have a solid background"
                ),
            )
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

        LithoColorHookPatch.lithoColorOverrideHook(INTEGRATIONS_CLASS_DESCRIPTOR, "getValue")
    }
}
