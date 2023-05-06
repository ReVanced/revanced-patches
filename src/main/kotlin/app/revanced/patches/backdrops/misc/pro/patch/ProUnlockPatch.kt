package app.revanced.patches.backdrops.misc.pro.patch

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
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.backdrops.misc.pro.annotations.ProUnlockCompatibility
import app.revanced.patches.backdrops.misc.pro.fingerprints.ProUnlockFingerprint
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction

@Patch
@Name("pro-unlock")
@Description("Unlocks pro-only functions.")
@ProUnlockCompatibility
@Version("0.0.1")
class ProUnlockPatch : BytecodePatch(
    listOf(ProUnlockFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        ProUnlockFingerprint.result?.let { result ->
            val registerIndex = result.scanResult.patternScanResult!!.endIndex - 1

            result.mutableMethod.apply {
                val register = instruction<OneRegisterInstruction>(registerIndex).registerA
                addInstructions(
                    result.scanResult.patternScanResult!!.endIndex,
                    """
                        const/4 v$register, 0x1
                    """
                )
            }

        } ?: return ProUnlockFingerprint.toErrorResult()

        return PatchResultSuccess()
    }
}