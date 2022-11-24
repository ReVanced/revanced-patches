package app.revanced.patches.youtube.layout.watchinvr.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.layout.watchinvr.annotations.WatchinVRCompatibility
import app.revanced.patches.youtube.layout.watchinvr.fingerprints.WatchinVRFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.shared.settings.impl.StringResource
import app.revanced.patches.shared.settings.impl.SwitchPreference

@Patch
@DependsOn([IntegrationsPatch::class, SettingsPatch::class])
@Name("hide-watch-in-vr")
@Description("Hides the Watch in VR option from the player settings flyout panel.")
@WatchinVRCompatibility
@Version("0.0.1")
class WatchinVRPatch : BytecodePatch(
    listOf(
        WatchinVRFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_hide_watch_in_vr",
                StringResource("revanced_hide_watch_in_vr_title", "Hide Watch in VR option"),
                false,
                StringResource("revanced_hide_watch_in_vr_summary_on", "Watch in VR option is hidden"),
                StringResource("revanced_hide_watch_in_vr_summary_off", "Watch in VR option is shown")
            )
        )

        WatchinVRFingerprint.result!!.mutableMethod.addInstructions(
            0, """
                invoke-static {}, Lapp/revanced/integrations/patches/HideWatchinVRPatch;->hideWatchinVR()Z
                move-result v0
                if-eqz v0, :shown
                return-void
                :shown
                nop
                """
        )

        return PatchResultSuccess()
    }
}