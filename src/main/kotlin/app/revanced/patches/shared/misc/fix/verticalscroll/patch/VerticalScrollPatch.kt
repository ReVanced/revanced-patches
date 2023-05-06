package app.revanced.patches.shared.misc.fix.verticalscroll.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patches.shared.misc.fix.verticalscroll.annotations.VerticalScrollCompatibility
import app.revanced.patches.shared.misc.fix.verticalscroll.fingerprints.CanScrollVerticallyFingerprint
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction

@Description("Fixes issues with scrolling on the home screen when the first component is of type EmptyComponent.")
@VerticalScrollCompatibility
@Version("0.0.1")
class VerticalScrollPatch : BytecodePatch(
    listOf(CanScrollVerticallyFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        CanScrollVerticallyFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = it.scanResult.patternScanResult!!.endIndex
                val moveResultRegister = instruction<OneRegisterInstruction>(insertIndex - 1).registerA

                addInstruction(
                    insertIndex,
                    "const/4 v$moveResultRegister, 0x0"
                )
            }
        } ?: return CanScrollVerticallyFingerprint.toErrorResult()

        return PatchResultSuccess()
    }
}
