package app.revanced.patches.youtube.layout.buttons.player.hide.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.layout.buttons.player.hide.annotations.HidePlayerButtonsCompatibility
import app.revanced.patches.youtube.layout.buttons.player.hide.fingerprints.PlayerControlsVisibilityModelFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import org.jf.dexlib2.iface.instruction.formats.Instruction3rc

@Patch
@DependsOn([IntegrationsPatch::class, SettingsPatch::class])
@Name("hide-player-buttons")
@Description("Adds the option to hide video player previous and next buttons.")
@HidePlayerButtonsCompatibility
@Version("0.0.1")
class HidePlayerButtonsPatch : BytecodePatch(
    listOf(PlayerControlsVisibilityModelFingerprint)
) {
    private object ParameterOffsets {
        const val HAS_NEXT = 5
    }

    override fun execute(context: BytecodeContext): PatchResult {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_hide_player_buttons",
                StringResource(
                    "revanced_hide_player_buttons_title",
                    "Hide previous & next video buttons"
                ),
                false,
                StringResource(
                    "revanced_hide_player_buttons_summary_on",
                    "Buttons are hidden"
                ),
                StringResource(
                    "revanced_hide_player_buttons_summary_off",
                    "Buttons are shown"
                )
            )
        )

        PlayerControlsVisibilityModelFingerprint.result?.apply {
            val callIndex = scanResult.patternScanResult!!.endIndex
            val callInstruction = mutableMethod.instruction(callIndex) as Instruction3rc

            // overriding this parameter register hides the previous and next buttons
            val hasNextParameterRegister = callInstruction.startRegister + ParameterOffsets.HAS_NEXT

            mutableMethod.addInstructions(
                callIndex,
                """
                    invoke-static { }, Lapp/revanced/integrations/patches/HidePlayerButtonsPatch;->hideButtons()Z
                    move-result v$hasNextParameterRegister
                    xor-int/lit8 v$hasNextParameterRegister, v$hasNextParameterRegister, 1
                """
            )
        } ?: return PlayerControlsVisibilityModelFingerprint.toErrorResult()
        return PatchResultSuccess()
    }
}
