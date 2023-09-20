package app.revanced.patches.youtube.layout.startupshortsreset

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.layout.startupshortsreset.fingerprints.UserWasInShortsFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(
    name = "Disable Shorts on startup",
    description = "Disables playing YouTube Shorts when launching YouTube.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube", [
                "18.16.37",
                "18.19.35",
                "18.20.39",
                "18.23.35",
                "18.29.38",
                "18.32.39"
            ]
        )
    ]
)
@Suppress("unused")
object DisableShortsOnStartupPatch : BytecodePatch(
    setOf(UserWasInShortsFingerprint)
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
