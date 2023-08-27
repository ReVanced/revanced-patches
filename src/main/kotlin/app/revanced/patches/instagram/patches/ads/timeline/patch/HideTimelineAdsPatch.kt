package app.revanced.patches.instagram.patches.ads.timeline.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.*
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.instagram.patches.ads.timeline.fingerprints.MediaFingerprint
import app.revanced.patches.instagram.patches.ads.timeline.fingerprints.ShowAdFingerprint
import app.revanced.patches.instagram.patches.ads.timeline.fingerprints.ads.GenericMediaAdFingerprint
import app.revanced.patches.instagram.patches.ads.timeline.fingerprints.ads.MediaAdFingerprint
import app.revanced.patches.instagram.patches.ads.timeline.fingerprints.ads.PaidPartnershipAdFingerprint
import app.revanced.patches.instagram.patches.ads.timeline.fingerprints.ads.ShoppingAdFingerprint
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch
@Name("Hide timeline ads")
@Description("Removes ads from the timeline.")
@Compatibility([Package("com.instagram.android", arrayOf("275.0.0.27.98"))])
class HideTimelineAdsPatch : BytecodePatch(
    listOf(
        ShowAdFingerprint,
        MediaFingerprint,
        PaidPartnershipAdFingerprint // Unlike the other ads this one is resolved from all classes.
    )
) {
    override fun execute(context: BytecodeContext) {
        // region Resolve required methods to check for ads.

        ShowAdFingerprint.result ?: throw ShowAdFingerprint.exception

        PaidPartnershipAdFingerprint.result ?: throw PaidPartnershipAdFingerprint.exception

        MediaFingerprint.result?.let {
            GenericMediaAdFingerprint.resolve(context, it.classDef)
            ShoppingAdFingerprint.resolve(context, it.classDef)

            return@let
        } ?: throw MediaFingerprint.exception

        // endregion

        ShowAdFingerprint.result!!.apply {
            // region Create instructions.

            val scanStart = scanResult.patternScanResult!!.startIndex
            val jumpIndex = scanStart - 1

            val mediaInstanceRegister = mutableMethod.getInstruction<FiveRegisterInstruction>(scanStart).registerC
            val freeRegister = mutableMethod.getInstruction<OneRegisterInstruction>(jumpIndex).registerA

            val returnFalseLabel = "an_ad"

            val checkForAdInstructions =
                listOf(GenericMediaAdFingerprint, PaidPartnershipAdFingerprint, ShoppingAdFingerprint)
                    .map(MediaAdFingerprint::toString)
                    .joinToString("\n") {
                        """ 
                            invoke-virtual {v$mediaInstanceRegister}, $it
                            move-result v$freeRegister
                            if-nez v$freeRegister, :$returnFalseLabel
                        """.trimIndent()
                    }.let { "$it\nconst/4 v0, 0x1\nreturn v0" }

            // endregion

            // region Patch.

            val insertIndex = scanStart + 3

            mutableMethod.addInstructionsWithLabels(
                insertIndex,
                checkForAdInstructions,
                ExternalLabel(
                    returnFalseLabel,
                    mutableMethod.getInstruction(mutableMethod.implementation!!.instructions.size - 2 /* return false = ad */)
                )
            )

            // endregion

            // region Jump to checks for ads from previous patch.

            mutableMethod.apply {
                addInstructionsWithLabels(
                    jumpIndex + 1,
                    "if-nez v$freeRegister, :start_check",
                    ExternalLabel("start_check", getInstruction(insertIndex))
                )
            }.removeInstruction(jumpIndex)

            // endregion
        }
    }
}