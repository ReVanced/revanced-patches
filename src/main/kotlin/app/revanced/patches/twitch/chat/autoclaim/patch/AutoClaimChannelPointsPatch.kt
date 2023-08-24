package app.revanced.patches.twitch.chat.autoclaim.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.twitch.chat.autoclaim.annotations.AutoClaimChannelPointsCompatibility
import app.revanced.patches.twitch.chat.autoclaim.fingerprints.CommunityPointsButtonViewDelegateFingerprint
import app.revanced.patches.twitch.misc.settings.bytecode.patch.SettingsPatch

@Patch
@DependsOn([SettingsPatch::class])
@Name("Auto claim channel points")
@Description("Automatically claim Channel Points.")
@AutoClaimChannelPointsCompatibility
class AutoClaimChannelPointPatch : BytecodePatch(
    listOf(CommunityPointsButtonViewDelegateFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        SettingsPatch.PreferenceScreen.CHAT.GENERAL.addPreferences(
            SwitchPreference(
                "revanced_auto_claim_channel_points",
                StringResource(
                    "revanced_auto_claim_channel_points",
                    "Automatically claim Channel Points"
                ),
                StringResource(
                    "revanced_auto_claim_channel_points_on",
                    "Channel Points are claimed automatically"
                ),
                StringResource(
                    "revanced_auto_claim_channel_points_off",
                    "Channel Points are not claimed automatically"
                ),
                default = true
            )
        )

        CommunityPointsButtonViewDelegateFingerprint.result?.mutableMethod?.apply {
            val lastIndex = implementation!!.instructions.lastIndex
            addInstructionsWithLabels(
                lastIndex, // place in front of return-void
                """
                    invoke-static {}, Lapp/revanced/twitch/patches/AutoClaimChannelPointsPatch;->shouldAutoClaim()Z
                    move-result v0
                    if-eqz v0, :auto_claim

                    # Claim by calling the button's onClick method

                    iget-object v0, p0, Ltv/twitch/android/shared/community/points/viewdelegate/CommunityPointsButtonViewDelegate;->buttonLayout:Landroid/view/ViewGroup;
                    invoke-virtual { v0 }, Landroid/view/View;->callOnClick()Z
                """,
                ExternalLabel("auto_claim", getInstruction(lastIndex))
            )
        } ?: throw CommunityPointsButtonViewDelegateFingerprint.exception
    }
}
