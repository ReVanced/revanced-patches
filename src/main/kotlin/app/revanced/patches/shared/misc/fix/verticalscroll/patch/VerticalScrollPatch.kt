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
        val result = CanScrollVerticallyFingerprint.result ?: return CanScrollVerticallyFingerprint.toErrorResult()

        with(result) {
            val method = mutableMethod

            val moveResultIndex = scanResult.patternScanResult!!.endIndex
            val moveResultRegister = (method.instruction(moveResultIndex) as OneRegisterInstruction).registerA

            method.addInstruction(
                moveResultIndex + 1,
                "const/4 v$moveResultRegister, 0x0"
            )
        }

        return PatchResultSuccess()
    }
}
