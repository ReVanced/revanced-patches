package app.revanced.patches.youtube.layout.theme.bytecode.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.*
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.layout.seekbar.bytecode.patch.SeekbarColorBytecodePatch
import app.revanced.patches.youtube.layout.theme.annotations.ThemeCompatibility
import app.revanced.patches.youtube.layout.theme.resource.ThemeResourcePatch

@Patch
@Name("Theme")
@Description("Applies a custom theme.")
@DependsOn([LithoColorHookPatch::class, SeekbarColorBytecodePatch::class, ThemeResourcePatch::class])
@ThemeCompatibility
class ThemeBytecodePatch : BytecodePatch() {

    override fun execute(context: BytecodeContext) {
        LithoColorHookPatch.lithoColorOverrideHook(INTEGRATIONS_CLASS_DESCRIPTOR, "getValue")
    }

    companion object : OptionsContainer() {
        private const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/patches/theme/ThemeLithoComponentsPatch;"

        var darkThemeBackgroundColor: String? by option(
            PatchOption.StringOption(
                key = "darkThemeBackgroundColor",
                default = "@android:color/black",
                title = "Background color for the dark theme",
                description = "The background color of the dark theme. Can be a hex color or a resource reference.",
            )
        )

        var lightThemeBackgroundColor: String? by option(
            PatchOption.StringOption(
                key = "lightThemeBackgroundColor",
                default = "@android:color/white",
                title = "Background color for the light theme",
                description = "The background color of the light theme. Can be a hex color or a resource reference.",
            )
        )
    }
}
