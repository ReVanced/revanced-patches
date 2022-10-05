package app.revanced.patches.youtube.layout.playerpopuppanels.patch

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
import app.revanced.patches.youtube.layout.playerpopuppanels.annotations.PlayerPopupPanelsCompatibility
import app.revanced.patches.youtube.layout.playerpopuppanels.fingerprints.EngagementPanelControllerFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.settings.framework.components.impl.StringResource
import app.revanced.patches.youtube.misc.settings.framework.components.impl.SwitchPreference

@Patch
@DependsOn([IntegrationsPatch::class, SettingsPatch::class])
@Name("disable-auto-player-popup-panels")
@Description("Disable automatic popup panels (playlist or live chat) on video player.")
@PlayerPopupPanelsCompatibility
@Version("0.0.1")
class PlayerPopupPanelsPatch : BytecodePatch(
    listOf(
        EngagementPanelControllerFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_player_popup_panels_enabled",
                StringResource("revanced_player_popup_panels_title", "Disable player popup panels"),
                false,
                StringResource("revanced_player_popup_panels_summary_on", "Player popup panels are disabled"),
                StringResource("revanced_player_popup_panels_summary_off", "Player popup panels are enabled")
            )
        )

        val engagementPanelControllerMethod = EngagementPanelControllerFingerprint.result!!.mutableMethod

        engagementPanelControllerMethod.addInstructions(
            0, """
            invoke-static { }, Lapp/revanced/integrations/patches/DisablePlayerPopupPanelsPatch;->disablePlayerPopupPanels()Z
            move-result v0
            if-eqz v0, :player_popup_panels
            if-eqz p4, :player_popup_panels
            const/4 v0, 0x0
            return-object v0
            :player_popup_panels
            nop
        """
        )

        return PatchResultSuccess()
    }
}
