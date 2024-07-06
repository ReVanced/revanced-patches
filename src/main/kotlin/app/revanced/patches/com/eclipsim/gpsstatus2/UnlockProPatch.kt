package app.revanced.patches.com.eclipsim.gpsstatus2

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.com.eclipsim.gpsstatus2.fingerprints.AppSignatureQueryFingerprint
import app.revanced.patches.com.eclipsim.gpsstatus2.fingerprints.LoadPreferencesFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Unlock PRO",
    compatiblePackages = [CompatiblePackage("com.eclipsim.gpsstatus2")]
)
@Suppress("unused")
object UnlockProPatch : BytecodePatch(
    setOf(LoadPreferencesFingerprint, AppSignatureQueryFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        val activationNumber = -0x2ea88
        LoadPreferencesFingerprint.result?.let { result ->
            val constIndex = result.scanResult.patternScanResult!!.startIndex
            val invokeInterfaceIndex = result.scanResult.patternScanResult!!.startIndex + 1
            result.mutableMethod.apply {
                val register = getInstruction<OneRegisterInstruction>(constIndex).registerA
                replaceInstruction(
                    constIndex,
                    "const-wide/32 v$register, $activationNumber"
                )
                removeInstructions(invokeInterfaceIndex, 3)
            }
        } ?: throw LoadPreferencesFingerprint.exception

        val appSignatureConstant =
            "4981e0ca:CN=EclipSim,OU=EclipSim,O=EclipSim,L=Unknown,ST=Unknown,C=Unknown".hashCode() % 1000000
        AppSignatureQueryFingerprint.result?.let { result ->
            result.mutableMethod.apply {
                val register =
                    getInstruction<OneRegisterInstruction>(result.scanResult.patternScanResult!!.endIndex).registerA
                addInstructions(
                    result.scanResult.patternScanResult!!.endIndex,
                    "const-wide/32 v$register, $appSignatureConstant"
                )
                removeInstructions(result.scanResult.patternScanResult!!.startIndex, 6)
            }
        } ?: throw AppSignatureQueryFingerprint.exception
    }
}
