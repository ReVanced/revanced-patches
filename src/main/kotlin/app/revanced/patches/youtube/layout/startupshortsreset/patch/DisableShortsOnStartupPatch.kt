package app.revanced.patches.youtube.layout.startupshortsreset.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.layout.startupshortsreset.annotations.StartupShortsResetCompatibility
import app.revanced.patches.youtube.layout.startupshortsreset.fingerprints.UserWasInShortsFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch

@Patch
@DependsOn([IntegrationsPatch::class, SettingsPatch::class])
@Name("Disable Shorts on startup")
@Description("Disables playing YouTube Shorts when launching YouTube.")
@StartupShortsResetCompatibility
class DisableShortsOnStartupPatch : BytecodePatch(
    listOf(
        UserWasInShortsFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_disable_resuming_shorts_player",
                StringResource("revanced_disable_resuming_shorts_player_title", "Disable Shorts player at app startup"),
                StringResource("revanced_disable_resuming_shorts_player_summary_on", "Shorts player is disabled at app startup"),
                StringResource("revanced_disable_resuming_shorts_player_summary_off", "Shorts player is enabled at app startup")
            )
        )

        val userWasInShortsResult = UserWasInShortsFingerprint.result!!
        val userWasInShortsMethod = userWasInShortsResult.mutableMethod
        val moveResultIndex = userWasInShortsResult.scanResult.patternScanResult!!.endIndex

        userWasInShortsMethod.addInstructionsWithLabels(
            moveResultIndex + 1,
            """
                invoke-static { }, Lapp/revanced/integrations/patches/DisableStartupShortsPlayerPatch;->disableStartupShortsPlayer()Z
                move-result v5
                if-eqz v5, :disable_shorts_player
                return-void
                :disable_shorts_player
                nop
            """
        )
    }
}
