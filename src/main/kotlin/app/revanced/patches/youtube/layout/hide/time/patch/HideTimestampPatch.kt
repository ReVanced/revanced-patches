package app.revanced.patches.youtube.layout.hide.time.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.BytecodePatch
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
@Name("Hide timestamp")
@Description("Hides timestamp in video player.")
@HideTimeCompatibility
class HideTimestampPatch : BytecodePatch(
    listOf(
        TimeCounterFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_hide_timestamp",
                StringResource("revanced_hide_timestamp_title", "Hide video timestamp"),
                StringResource("revanced_hide_timestamp_summary_on", "Timestamp is hidden"),
                StringResource("revanced_hide_timestamp_summary_off", "Timestamp is shown")
            )
        )

        TimeCounterFingerprint.result?.apply {
            mutableMethod.addInstructionsWithLabels(
            0,
            """
                invoke-static { }, Lapp/revanced/integrations/patches/HideTimestampPatch;->hideTimestamp()Z
                move-result v0
                if-eqz v0, :hide_time
                return-void
                :hide_time
                nop
            """
            )
        } ?: throw TimeCounterFingerprint.exception
    }
}
