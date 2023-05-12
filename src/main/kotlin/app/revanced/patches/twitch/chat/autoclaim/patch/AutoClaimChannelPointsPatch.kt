package app.revanced.patches.twitch.chat.autoclaim.patch

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
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.twitch.chat.autoclaim.annotations.AutoClaimChannelPointsCompatibility
import app.revanced.patches.twitch.chat.autoclaim.fingerprints.CommunityPointsButtonViewDelegateFingerprint
import app.revanced.patches.twitch.misc.settings.bytecode.patch.SettingsPatch

@Patch
@DependsOn([SettingsPatch::class])
@Name("auto-claim-channel-points")
@Description("Automatically claim channel points.")
@AutoClaimChannelPointsCompatibility
@Version("0.0.1")
class AutoClaimChannelPointPatch : BytecodePatch(
    listOf(CommunityPointsButtonViewDelegateFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        SettingsPatch.PreferenceScreen.CHAT.GENERAL.addPreferences(
            SwitchPreference(
                "revanced_auto_claim_channel_points",
                StringResource(
                    "revanced_auto_claim_channel_points",
                    "Automatically claim channel points"
                ),
                true,
                StringResource(
                    "revanced_auto_claim_channel_points_on",
                    "Channel points are claimed automatically"
                ),
                StringResource(
                    "revanced_auto_claim_channel_points_off",
                    "Channel points are not claimed automatically"
                ),
            )
        )

        CommunityPointsButtonViewDelegateFingerprint.result?.mutableMethod?.apply {
            val lastIndex = implementation!!.instructions.lastIndex
            addInstructions(
                lastIndex, // place in front of return-void
                """
                    invoke-static {}, Lapp/revanced/twitch/patches/AutoClaimChannelPointsPatch;->shouldAutoClaim()Z
                    move-result v0
                    if-eqz v0, :auto_claim

                    # Claim by calling the button's onClick method

                    iget-object v0, p0, Ltv/twitch/android/shared/community/points/viewdelegate/CommunityPointsButtonViewDelegate;->buttonLayout:Landroid/view/ViewGroup;
                    invoke-virtual { v0 }, Landroid/view/View;->callOnClick()Z
                """,
                listOf(ExternalLabel("auto_claim", instruction(lastIndex)))
            )
        } ?: return CommunityPointsButtonViewDelegateFingerprint.toErrorResult()
        return PatchResultSuccess()
    }
}
