package app.revanced.patches.youtube.layout.startupshortsreset.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patches.youtube.layout.startupshortsreset.annotations.StartupShortsResetCompatibility
import app.revanced.patches.youtube.layout.startupshortsreset.fingerprints.ActionOpenShortsFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.settings.framework.components.impl.StringResource
import app.revanced.patches.youtube.misc.settings.framework.components.impl.SwitchPreference
import org.jf.dexlib2.builder.instruction.BuilderInstruction21c
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction
import org.jf.dexlib2.iface.instruction.TwoRegisterInstruction

@Patch
@DependsOn([IntegrationsPatch::class, SettingsPatch::class])
@Name("disable-startup-shorts-player")
@Description("Disables playing YouTube Shorts when launching YouTube.")
@StartupShortsResetCompatibility
@Version("0.0.1")
class DisableShortsOnStartupPatch : BytecodePatch(
    listOf(
        ActionOpenShortsFingerprint
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_startup_shorts_player_enabled",
                StringResource("revanced_startup_shorts_player_title", "Disable shorts player at app startup"),
                false,
                StringResource("revanced_startup_shorts_player_summary_on", "Shorts player is disabled at app startup"),
                StringResource("revanced_startup_shorts_player_summary_off", "Shorts player is enabled at app startup")
            )
        )

        val actionOpenShortsResult = ActionOpenShortsFingerprint.result
        val actionOpenShortsMethod = actionOpenShortsResult!!.mutableMethod
        val actionOpenShortsInstructions = actionOpenShortsMethod.implementation!!.instructions

        val moveResultIndex = actionOpenShortsResult.scanResult.stringsScanResult!!.matches.first().index + 5
        val iPutBooleanIndex = moveResultIndex + 6

        val moveResultRegister = (actionOpenShortsInstructions[moveResultIndex] as OneRegisterInstruction).registerA
        val iPutBooleanRegister = (actionOpenShortsInstructions[iPutBooleanIndex] as TwoRegisterInstruction).registerA

        actionOpenShortsMethod.addInstructions(
            moveResultIndex + 1, """
            invoke-static { }, Lapp/revanced/integrations/patches/DisableStartupShortsPlayerPatch;->disableStartupShortsPlayer()Z
            move-result v$moveResultRegister
            if-nez v$moveResultRegister, :cond_startup_shorts_reset
            const/4 v$iPutBooleanRegister, 0x0
            :cond_startup_shorts_reset
            nop
        """
        )

        return PatchResultSuccess()
    }
}
