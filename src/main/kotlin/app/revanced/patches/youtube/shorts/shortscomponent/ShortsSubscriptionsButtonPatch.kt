package app.revanced.patches.youtube.shorts.shortscomponent

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.shorts.shortscomponent.fingerprints.ShortsSubscriptionsFingerprint
import app.revanced.patches.youtube.shorts.shortscomponent.fingerprints.ShortsSubscriptionsTabletFingerprint
import app.revanced.patches.youtube.shorts.shortscomponent.fingerprints.ShortsSubscriptionsTabletParentFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.SHORTS
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.ReelPlayerFooter
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.ReelPlayerPausedStateButton
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.util.exception
import app.revanced.util.getWideLiteralInstructionIndex
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

@Patch(dependencies = [SettingsPatch::class])
object ShortsSubscriptionsButtonPatch : BytecodePatch(
    setOf(
        ShortsSubscriptionsFingerprint,
        ShortsSubscriptionsTabletParentFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        ShortsSubscriptionsFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = getWideLiteralInstructionIndex(ReelPlayerPausedStateButton) + 2
                val insertRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                addInstruction(
                    insertIndex + 1,
                    "invoke-static {v$insertRegister}, $SHORTS->hideShortsPlayerSubscriptionsButton(Landroid/view/View;)V"
                )
            }
        } ?: throw ShortsSubscriptionsFingerprint.exception

        /**
         * Deprecated in YouTube v18.31.xx+
         */
        if (!SettingsPatch.upward1831) {
            ShortsSubscriptionsTabletParentFingerprint.result?.let { parentResult ->
                parentResult.mutableMethod.apply {
                    val targetIndex = getWideLiteralInstructionIndex(ReelPlayerFooter) - 1
                    if (getInstruction(targetIndex).opcode != Opcode.IPUT)
                        throw ShortsSubscriptionsTabletFingerprint.exception
                    subscriptionFieldReference =
                        (getInstruction<ReferenceInstruction>(targetIndex)).reference as FieldReference
                }

                ShortsSubscriptionsTabletFingerprint.also {
                    it.resolve(
                        context,
                        parentResult.classDef
                    )
                }.result?.mutableMethod?.let {
                    with(it.implementation!!.instructions) {
                        filter { instruction ->
                            val fieldReference =
                                (instruction as? ReferenceInstruction)?.reference as? FieldReference
                            instruction.opcode == Opcode.IGET && fieldReference == subscriptionFieldReference
                        }.forEach { instruction ->
                            val insertIndex = indexOf(instruction) + 1
                            val register = (instruction as TwoRegisterInstruction).registerA

                            it.addInstructions(
                                insertIndex, """
                                invoke-static {v$register}, $SHORTS->hideShortsPlayerSubscriptionsButton(I)I
                                move-result v$register
                                """
                            )
                        }
                    }
                } ?: throw ShortsSubscriptionsTabletFingerprint.exception
            } ?: throw ShortsSubscriptionsTabletParentFingerprint.exception
        }
    }

    private lateinit var subscriptionFieldReference: FieldReference
}
