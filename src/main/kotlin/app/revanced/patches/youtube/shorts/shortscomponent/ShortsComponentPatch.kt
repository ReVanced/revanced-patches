package app.revanced.patches.youtube.shorts.shortscomponent

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.shorts.shortscomponent.fingerprints.ShortsCommentFingerprint
import app.revanced.patches.youtube.shorts.shortscomponent.fingerprints.ShortsDislikeFingerprint
import app.revanced.patches.youtube.shorts.shortscomponent.fingerprints.ShortsInfoPanelFingerprint
import app.revanced.patches.youtube.shorts.shortscomponent.fingerprints.ShortsLikeFingerprint
import app.revanced.patches.youtube.shorts.shortscomponent.fingerprints.ShortsPaidPromotionFingerprint
import app.revanced.patches.youtube.shorts.shortscomponent.fingerprints.ShortsPivotFingerprint
import app.revanced.patches.youtube.shorts.shortscomponent.fingerprints.ShortsPivotLegacyFingerprint
import app.revanced.patches.youtube.shorts.shortscomponent.fingerprints.ShortsRemixFingerprint
import app.revanced.patches.youtube.shorts.shortscomponent.fingerprints.ShortsShareFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.COMPONENTS_PATH
import app.revanced.patches.youtube.utils.integrations.Constants.SHORTS
import app.revanced.patches.youtube.utils.litho.LithoFilterPatch
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.ReelDynRemix
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.ReelDynShare
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.ReelForcedMuteButton
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.ReelPivotButton
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.ReelPlayerBadge
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.ReelPlayerBadge2
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.ReelPlayerInfoPanel
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.ReelRightDislikeIcon
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.ReelRightLikeIcon
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.RightComment
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.util.exception
import app.revanced.util.getTargetIndex
import app.revanced.util.getTargetIndexReversed
import app.revanced.util.getWideLiteralInstructionIndex
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Hide shorts components",
    description = "Adds options to hide components related to YouTube Shorts.",
    dependencies = [
        LithoFilterPatch::class,
        SettingsPatch::class,
        SharedResourceIdPatch::class,
        ShortsNavigationBarPatch::class,
        ShortsSubscriptionsButtonPatch::class,
        ShortsToolBarPatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.25.40",
                "18.27.36",
                "18.29.38",
                "18.30.37",
                "18.31.40",
                "18.32.39",
                "18.33.40",
                "18.34.38",
                "18.35.36",
                "18.36.39",
                "18.37.36",
                "18.38.44",
                "18.39.41",
                "18.40.34",
                "18.41.39",
                "18.42.41",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.46.45",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39"
            ]
        )
    ]
)
@Suppress("unused")
object ShortsComponentPatch : BytecodePatch(
    setOf(
        ShortsCommentFingerprint,
        ShortsDislikeFingerprint,
        ShortsInfoPanelFingerprint,
        ShortsLikeFingerprint,
        ShortsPaidPromotionFingerprint,
        ShortsPivotFingerprint,
        ShortsPivotLegacyFingerprint,
        ShortsRemixFingerprint,
        ShortsShareFingerprint,
    )
) {
    override fun execute(context: BytecodeContext) {

        /**
         * Comment button
         */
        ShortsCommentFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = getWideLiteralInstructionIndex(RightComment) + 3

                hideButton(insertIndex, 1, "hideShortsPlayerCommentsButton")
            }
        } ?: throw ShortsCommentFingerprint.exception

        /**
         * Dislike button
         */
        ShortsDislikeFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = getWideLiteralInstructionIndex(ReelRightDislikeIcon)
                val insertRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                val jumpIndex = getTargetIndex(insertIndex, Opcode.CONST_CLASS) + 2

                addInstructionsWithLabels(
                    insertIndex + 1, """
                        invoke-static {}, $SHORTS->hideShortsPlayerDislikeButton()Z
                        move-result v$insertRegister
                        if-nez v$insertRegister, :hide
                        const v$insertRegister, $ReelRightDislikeIcon
                        """, ExternalLabel("hide", getInstruction(jumpIndex))
                )
            }
        } ?: throw ShortsDislikeFingerprint.exception

        /**
         * Info panel
         */
        ShortsInfoPanelFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = getWideLiteralInstructionIndex(ReelPlayerInfoPanel) + 3

                hideButtons(
                    insertIndex,
                    1,
                    "hideShortsPlayerInfoPanel(Landroid/view/ViewGroup;)Landroid/view/ViewGroup;"
                )
            }
        } ?: throw ShortsInfoPanelFingerprint.exception

        /**
         * Like button
         */
        ShortsLikeFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = getWideLiteralInstructionIndex(ReelRightLikeIcon)

                val insertRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                val jumpIndex = getTargetIndex(insertIndex, Opcode.CONST_CLASS) + 2

                addInstructionsWithLabels(
                    insertIndex + 1, """
                        invoke-static {}, $SHORTS->hideShortsPlayerLikeButton()Z
                        move-result v$insertRegister
                        if-nez v$insertRegister, :hide
                        const v$insertRegister, $ReelRightLikeIcon
                        """, ExternalLabel("hide", getInstruction(jumpIndex))
                )
            }
        } ?: throw ShortsLikeFingerprint.exception

        /**
         * Paid promotion
         */
        ShortsPaidPromotionFingerprint.result?.let {
            it.mutableMethod.apply {
                val primaryIndex = getWideLiteralInstructionIndex(ReelPlayerBadge) + 3
                val secondaryIndex = getWideLiteralInstructionIndex(ReelPlayerBadge2) + 3

                if (primaryIndex > secondaryIndex) {
                    hideButtons(
                        primaryIndex,
                        1,
                        "hideShortsPlayerPaidPromotionBanner(Landroid/view/ViewStub;)Landroid/view/ViewStub;"
                    )
                    hideButtons(
                        secondaryIndex,
                        1,
                        "hideShortsPlayerPaidPromotionBanner(Landroid/view/ViewStub;)Landroid/view/ViewStub;"
                    )
                } else {
                    hideButtons(
                        secondaryIndex,
                        1,
                        "hideShortsPlayerPaidPromotionBanner(Landroid/view/ViewStub;)Landroid/view/ViewStub;"
                    )
                    hideButtons(
                        primaryIndex,
                        1,
                        "hideShortsPlayerPaidPromotionBanner(Landroid/view/ViewStub;)Landroid/view/ViewStub;"
                    )
                }
            }
        } ?: throw ShortsPaidPromotionFingerprint.exception

        /**
         * Pivot button
         */
        ShortsPivotLegacyFingerprint.result?.let {
            it.mutableMethod.apply {
                val targetIndex = getWideLiteralInstructionIndex(ReelForcedMuteButton)
                val targetRegister = getInstruction<OneRegisterInstruction>(targetIndex).registerA

                val insertIndex = getTargetIndexReversed(targetIndex, Opcode.IF_EQZ)
                val jumpIndex = getTargetIndex(targetIndex, Opcode.GOTO)

                addInstructionsWithLabels(
                    insertIndex, """
                        invoke-static {}, $SHORTS->hideShortsPlayerPivotButton()Z
                        move-result v$targetRegister
                        if-nez v$targetRegister, :hide
                        """, ExternalLabel("hide", getInstruction(jumpIndex))
                )
            }
        } ?: ShortsPivotFingerprint.result?.let {
            it.mutableMethod.apply {
                val targetIndex = getWideLiteralInstructionIndex(ReelPivotButton)
                val insertIndex = getTargetIndexReversed(targetIndex, Opcode.INVOKE_STATIC) + 2

                hideButtons(
                    insertIndex,
                    0,
                    "hideShortsPlayerPivotButton(Ljava/lang/Object;)Ljava/lang/Object;"
                )
            }
        } ?: throw ShortsPivotFingerprint.exception

        /**
         * Remix button
         */
        ShortsRemixFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = getWideLiteralInstructionIndex(ReelDynRemix) - 2

                hideButton(insertIndex, 0, "hideShortsPlayerRemixButton")
            }
        } ?: throw ShortsRemixFingerprint.exception

        /**
         * Share button
         */
        ShortsShareFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = getWideLiteralInstructionIndex(ReelDynShare) - 2

                hideButton(insertIndex, 0, "hideShortsPlayerShareButton")
            }
        } ?: throw ShortsShareFingerprint.exception

        LithoFilterPatch.addFilter("$COMPONENTS_PATH/ShortsFilter;")

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: SHORTS_SETTINGS",
                "SETTINGS: HIDE_SHORTS_SHELF",
                "SETTINGS: SHORTS_PLAYER_PARENT",
                "SETTINGS: HIDE_SHORTS_COMPONENTS"
            )
        )

        SettingsPatch.updatePatchStatus("Hide shorts components")

    }

    private fun MutableMethod.hideButton(
        insertIndex: Int,
        offset: Int,
        descriptor: String
    ) {
        val insertRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

        addInstruction(
            insertIndex + offset,
            "invoke-static {v$insertRegister}, $SHORTS->$descriptor(Landroid/view/View;)V"
        )
    }

    private fun MutableMethod.hideButtons(
        insertIndex: Int,
        offset: Int,
        descriptor: String
    ) {
        val insertRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

        addInstructions(
            insertIndex + offset, """
                invoke-static {v$insertRegister}, $SHORTS->$descriptor
                move-result-object v$insertRegister
                """
        )
    }
}
