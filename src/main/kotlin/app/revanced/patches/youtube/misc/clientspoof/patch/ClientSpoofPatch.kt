package app.revanced.patches.youtube.misc.clientspoof.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.misc.clientspoof.annotations.ClientSpoofCompatibility
import app.revanced.patches.youtube.misc.clientspoof.fingerprints.UserAgentHeaderBuilderFingerprint
import org.jf.dexlib2.iface.instruction.FiveRegisterInstruction

@Patch
@Name("client-spoof")
@Description("Spoofs the YouTube or Vanced client to prevent playback issues.")
@ClientSpoofCompatibility
@Version("0.0.1")
class ClientSpoofPatch : BytecodePatch(
    listOf(UserAgentHeaderBuilderFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        val result = UserAgentHeaderBuilderFingerprint.result!!
        val method = result.mutableMethod

        val insertIndex = result.scanResult.patternScanResult!!.endIndex
        val packageNameRegister = (method.instruction(insertIndex) as FiveRegisterInstruction).registerD

        val originalPackageName = "com.google.android.youtube"
        method.addInstruction(insertIndex, "const-string v$packageNameRegister, \"$originalPackageName\"")

        return PatchResultSuccess()
    }
}
