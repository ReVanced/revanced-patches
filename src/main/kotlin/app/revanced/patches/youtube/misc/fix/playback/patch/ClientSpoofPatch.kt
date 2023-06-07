package app.revanced.patches.youtube.misc.fix.playback.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.misc.fix.playback.annotations.ClientSpoofCompatibility
import app.revanced.patches.youtube.misc.fix.playback.fingerprints.UserAgentHeaderBuilderFingerprint
import org.jf.dexlib2.iface.instruction.FiveRegisterInstruction

@Patch
@Name("client-spoof")
@Description("Spoofs a patched client to allow playback.")
@ClientSpoofCompatibility
@DependsOn([SpoofSignatureVerificationPatch::class])
@Version("0.0.1")
class ClientSpoofPatch : BytecodePatch(
    listOf(UserAgentHeaderBuilderFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        UserAgentHeaderBuilderFingerprint.result?.let { result ->
            val insertIndex = result.scanResult.patternScanResult!!.endIndex
           result.mutableMethod.apply {
               val packageNameRegister = getInstruction<FiveRegisterInstruction>(insertIndex).registerD

               addInstruction(insertIndex, "const-string v$packageNameRegister, \"$ORIGINAL_PACKAGE_NAME\"")
           }

        } ?: return UserAgentHeaderBuilderFingerprint.toErrorResult()

        return PatchResultSuccess()
    }

    private companion object {
        private const val ORIGINAL_PACKAGE_NAME = "com.google.android.youtube"
    }
}
