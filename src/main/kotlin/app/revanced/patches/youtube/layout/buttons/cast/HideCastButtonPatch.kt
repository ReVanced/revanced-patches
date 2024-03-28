package app.revanced.patches.youtube.layout.buttons.cast

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(
    name = "Hide cast button",
    description = "Adds an option to hide the cast button in the video player.",
    dependencies = [
        IntegrationsPatch::class,
        SettingsPatch::class,
        AddResourcesPatch::class,
    ],
    compatiblePackages = [
        CompatiblePackage("com.google.android.youtube"),
    ],
)
object HideCastButtonPatch : BytecodePatch(emptySet()) {
    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_hide_cast_button")
        )

        val buttonClass = context.findClass("MediaRouteButton")
            ?: throw PatchException("MediaRouteButton class not found.")

        buttonClass.mutableClass.methods.find { it.name == "setVisibility" }?.apply {
            addInstructions(
                0,
                """
                    invoke-static {p1}, Lapp/revanced/integrations/youtube/patches/HideCastButtonPatch;->getCastButtonOverrideV2(I)I
                    move-result p1
                """,
            )
        } ?: throw PatchException("setVisibility method not found.")
    }
}
