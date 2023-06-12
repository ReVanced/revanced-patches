package app.revanced.patches.shared.misc.fix.verticalscroll.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patches.shared.misc.fix.verticalscroll.annotations.VerticalScrollCompatibility
import app.revanced.patches.shared.misc.fix.verticalscroll.fingerprints.CanScrollVerticallyFingerprint
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction

@Description("Fixes issues with refreshing the feed when the first component is of type EmptyComponent.")
@VerticalScrollCompatibility
@Version("0.0.1")
class VerticalScrollPatch : BytecodePatch(
    listOf(CanScrollVerticallyFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        CanScrollVerticallyFingerprint.result?.let {
            it.mutableMethod.apply {
                val moveResultIndex = it.scanResult.patternScanResult!!.endIndex
                val moveResultRegister = getInstruction<OneRegisterInstruction>(moveResultIndex).registerA

                val insertIndex = moveResultIndex + 1
                addInstruction(
                    insertIndex,
                    "const/4 v$moveResultRegister, 0x0"
                )
            }
        } ?: return CanScrollVerticallyFingerprint.toErrorResult()

        return PatchResultSuccess()
    }
}
