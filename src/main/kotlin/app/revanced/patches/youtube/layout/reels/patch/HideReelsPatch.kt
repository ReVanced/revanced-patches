package app.revanced.patches.youtube.layout.reels.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.youtube.layout.reels.annotations.HideReelsCompatibility
import app.revanced.patches.youtube.layout.reels.fingerprints.HideReelsFingerprint
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.settings.framework.components.impl.StringResource
import app.revanced.patches.youtube.misc.settings.framework.components.impl.SwitchPreference

//@Patch TODO: this is currently in the general-bytecode-ads patch due to the integrations having a preference for including reels or not. Move it here.
@Name("hide-reels")
@Description("Hides reels on the home page.")
@DependsOn([SettingsPatch::class])
@HideReelsCompatibility
@Version("0.0.1")
class HideReelsPatch : BytecodePatch(
    listOf(
        HideReelsFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_reel_button_enabled",
                StringResource("revanced_reel_button_enabled_title", "Show reels button"),
                false,
                StringResource("revanced_reel_button_summary_on", "Reels button is shown"),
                StringResource("revanced_reel_button_summary_off", "Reels button is hidden")
            )
        )

        val result = HideReelsFingerprint.result!!

        // HideReel will hide the reel view before it is being used,
        // so we pass the view to the HideReel method
        result.mutableMethod.addInstruction(
            result.scanResult.patternScanResult!!.endIndex,
            "invoke-static { v2 }, Lapp/revanced/integrations/patches/HideReelsPatch;->HideReel(Landroid/view/View;)V"
        )

        return PatchResultSuccess()
    }
}
