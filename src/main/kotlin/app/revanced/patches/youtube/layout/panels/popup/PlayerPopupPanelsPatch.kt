package app.revanced.patches.youtube.layout.panels.popup

import app.revanced.util.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.layout.panels.popup.fingerprints.EngagementPanelControllerFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(
    name = "Disable player popup panels",
    description = "Adds an option to disable panels (such as live chat) from opening automatically.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube", [
                "18.32.39",
                "18.37.36",
                "18.38.44",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.48.39",
                "18.49.37",
                "19.01.34"
            ]
        )
    ]
)
@Suppress("unused")
object PlayerPopupPanelsPatch : BytecodePatch(
    setOf(EngagementPanelControllerFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_hide_player_popup_panels",
                StringResource("revanced_hide_player_popup_panels_title", "Hide player popup panels"),
                StringResource("revanced_hide_player_popup_panels_summary_on", "Player popup panels are hidden"),
                StringResource("revanced_hide_player_popup_panels_summary_off", "Player popup panels are shown")
            )
        )

        val engagementPanelControllerMethod = EngagementPanelControllerFingerprint
            .result?.mutableMethod ?: throw EngagementPanelControllerFingerprint.exception

        engagementPanelControllerMethod.addInstructionsWithLabels(
            0,
            """
                invoke-static { }, Lapp/revanced/integrations/youtube/patches/DisablePlayerPopupPanelsPatch;->disablePlayerPopupPanels()Z
                move-result v0
                if-eqz v0, :player_popup_panels
                if-eqz p4, :player_popup_panels
                const/4 v0, 0x0
                return-object v0
                :player_popup_panels
                nop
            """
        )
    }
}
