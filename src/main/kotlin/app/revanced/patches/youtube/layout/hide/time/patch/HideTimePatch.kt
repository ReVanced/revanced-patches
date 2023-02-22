package app.revanced.patches.youtube.layout.hide.time.patch

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
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.layout.hide.time.annotations.HideTimeCompatibility
import app.revanced.patches.youtube.layout.hide.time.fingerprints.TimeCounterFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch

@Patch
@DependsOn([IntegrationsPatch::class, SettingsPatch::class])
@Name("hide-time")
@Description("Hides the videos time.")
@HideTimeCompatibility
@Version("0.0.1")
class HideTimePatch : BytecodePatch(
    listOf(
        TimeCounterFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_hide_time",
                StringResource("revanced_hide_time_title", "Hide time"),
                false,
                StringResource("revanced_hide_time_summary_on", "Time is hidden"),
                StringResource("revanced_hide_time_summary_off", "Time is shown")
            )
        )

        TimeCounterFingerprint.result!!.mutableMethod.addInstructions(
            0, """
            invoke-static { }, Lapp/revanced/integrations/patches/HideTimePatch;->hideTime()Z
            move-result v0
            if-eqz v0, :hide_time
            return-void
            :hide_time
            nop
        """
        )

        return PatchResultSuccess()
    }
}
