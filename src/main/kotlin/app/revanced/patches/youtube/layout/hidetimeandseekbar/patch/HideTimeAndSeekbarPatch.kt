package app.revanced.patches.youtube.layout.hidetimeandseekbar.patch

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
import app.revanced.patches.youtube.layout.hidetimeandseekbar.annotations.HideTimeAndSeekbarCompatibility
import app.revanced.patches.youtube.layout.hidetimeandseekbar.fingerprints.TimeCounterFingerprint
import app.revanced.patches.youtube.layout.sponsorblock.bytecode.fingerprints.CreateVideoPlayerSeekbarFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.settings.framework.components.impl.StringResource
import app.revanced.patches.youtube.misc.settings.framework.components.impl.SwitchPreference

@Patch
@DependsOn([IntegrationsPatch::class, SettingsPatch::class])
@Name("hide-time-and-seekbar")
@Description("Hides progress bar and time counter on videos.")
@HideTimeAndSeekbarCompatibility
@Version("0.0.1")
class HideTimeAndSeekbarPatch : BytecodePatch(
    listOf(
        CreateVideoPlayerSeekbarFingerprint, TimeCounterFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_hide_time_and_seekbar",
                StringResource("revanced_hide_time_and_seekbar_title", "Hide time and seekbar"),
                false,
                StringResource("revanced_hide_time_and_seekbar_summary_on", "Time and seekbar are hidden"),
                StringResource("revanced_hide_time_and_seekbar_summary_off", "Time and seekbar are visible")
            )
        )

        val createVideoPlayerSeekbarMethod = CreateVideoPlayerSeekbarFingerprint.result!!.mutableMethod

        createVideoPlayerSeekbarMethod.addInstructions(
            0, """
            const/4 v0, 0x0
            invoke-static { }, Lapp/revanced/integrations/patches/HideTimeAndSeekbarPatch;->hideTimeAndSeekbar()Z
            move-result v0
            if-eqz v0, :hide_time_and_seekbar
            return-void
            :hide_time_and_seekbar
            nop
        """
        )

        val timeCounterMethod = TimeCounterFingerprint.result!!.mutableMethod

        timeCounterMethod.addInstructions(
            0, """
            invoke-static { }, Lapp/revanced/integrations/patches/HideTimeAndSeekbarPatch;->hideTimeAndSeekbar()Z
            move-result v0
            if-eqz v0, :hide_time_and_seekbar
            return-void
            :hide_time_and_seekbar
            nop
        """
        )

        return PatchResultSuccess()
    }
}
