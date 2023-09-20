package app.revanced.patches.youtube.layout.theme

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.types.StringPatchOption.Companion.stringPatchOption
import app.revanced.patches.youtube.layout.seekbar.SeekbarColorBytecodePatch

@Patch(
    name = "Theme",
    description = "Applies a custom theme.",
    dependencies = [LithoColorHookPatch::class, SeekbarColorBytecodePatch::class, ThemeResourcePatch::class],
    compatiblePackages = [
        CompatiblePackage("com.google.android.youtube")
    ]
)
@Suppress("unused")
object ThemeBytecodePatch : BytecodePatch() {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/patches/theme/ThemeLithoComponentsPatch;"

    internal val darkThemeBackgroundColor by stringPatchOption(
        key = "darkThemeBackgroundColor",
        default = "@android:color/black",
        title = "Background color for the dark theme",
        description = "The background color of the dark theme. Can be a hex color or a resource reference.",
    )

    internal val lightThemeBackgroundColor by stringPatchOption(
        key = "lightThemeBackgroundColor",
        default = "@android:color/white",
        title = "Background color for the light theme",
        description = "The background color of the light theme. Can be a hex color or a resource reference.",
    )

    override fun execute(context: BytecodeContext) {
        LithoColorHookPatch.lithoColorOverrideHook(INTEGRATIONS_CLASS_DESCRIPTOR, "getValue")
    }
}
