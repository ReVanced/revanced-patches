package app.revanced.patches.youtube.layout.panels.popup

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.layout.panels.popup.fingerprints.EngagementPanelControllerFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.exception

@Patch(
    name = "Disable player popup panels",
    description = "Adds an option to disable panels (such as live chat) from opening automatically.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class, AddResourcesPatch::class],
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
                "19.01.34",
                "19.02.39",
                "19.03.36",
                "19.04.38",
                "19.05.36",
                "19.06.39",
                "19.07.40",
                "19.08.36",
                "19.09.38",
                "19.10.39",
                "19.11.43"
            ]
        )
    ]
)
@Suppress("unused")
object PlayerPopupPanelsPatch : BytecodePatch(
    setOf(EngagementPanelControllerFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_hide_player_popup_panels")
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
