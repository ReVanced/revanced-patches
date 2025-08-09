package app.revanced.patches.twitch.chat.autoclaim

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.twitch.misc.settings.PreferenceScreen
import app.revanced.patches.twitch.misc.settings.settingsPatch

val autoClaimChannelPointsPatch = bytecodePatch(
    name = "Auto claim channel points",
    description = "Automatically claim Channel Points.",
) {
    dependsOn(
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith("tv.twitch.android.app"("16.9.1", "25.3.0"))

    execute {
        addResources("twitch", "chat.autoclaim.autoClaimChannelPointsPatch")

        PreferenceScreen.CHAT.GENERAL.addPreferences(
            SwitchPreference("revanced_auto_claim_channel_points"),
        )

        communityPointsButtonViewDelegateFingerprint.method.apply {
            val lastIndex = instructions.lastIndex
            addInstructionsWithLabels(
                lastIndex, // place in front of return-void
                """
                    invoke-static {}, Lapp/revanced/extension/twitch/patches/AutoClaimChannelPointsPatch;->shouldAutoClaim()Z
                    move-result v0
                    if-eqz v0, :auto_claim

                    # Claim by calling the button's onClick method

                    iget-object v0, p0, Ltv/twitch/android/shared/community/points/viewdelegate/CommunityPointsButtonViewDelegate;->buttonLayout:Landroid/view/ViewGroup;
                    invoke-virtual { v0 }, Landroid/view/View;->callOnClick()Z
                """,
                ExternalLabel("auto_claim", getInstruction(lastIndex)),
            )
        }
    }
}
