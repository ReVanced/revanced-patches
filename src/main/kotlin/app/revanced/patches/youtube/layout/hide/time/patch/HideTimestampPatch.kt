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
@Name("hide-timestamp")
@Description("Hides timestamp in video player.")
@HideTimeCompatibility
@Version("0.0.1")
class HideTimestampPatch : BytecodePatch(
    listOf(
        TimeCounterFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_hide_timestamp",
                StringResource("revanced_hide_timestamp_title", "Hide video timestamp"),
                false,
                StringResource("revanced_hide_timestamp_summary_on", "Timestamp is hidden"),
                StringResource("revanced_hide_timestamp_summary_off", "Timestamp is shown")
            )
        )

        TimeCounterFingerprint.result!!.mutableMethod.addInstructions(
            0, """
            invoke-static { }, Lapp/revanced/integrations/patches/HideTimestampPatch;->hideTimestamp()Z
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
