package app.revanced.patches.solidexplorer2.functionality.filesize.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Package
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.solidexplorer2.functionality.filesize.fingerprints.OnReadyFingerprint
import com.android.tools.smali.dexlib2.iface.instruction.ThreeRegisterInstruction

@Patch
@Name("Remove file size limit")
@Description("Allows opening files larger than 2 MB in the text editor.")
@Compatibility([Package("pl.solidexplorer2")])
class RemoveFileSizeLimitPatch : BytecodePatch(listOf(OnReadyFingerprint)) {
    override fun execute(context: BytecodeContext) = OnReadyFingerprint.result?.let { result ->
        val cmpIndex = result.scanResult.patternScanResult!!.startIndex + 1
        val cmpResultRegister = result.mutableMethod.getInstruction<ThreeRegisterInstruction>(cmpIndex).registerA

        result.mutableMethod.replaceInstruction(cmpIndex, "const/4 v${cmpResultRegister}, 0x0")
    } ?: throw OnReadyFingerprint.exception
}
