package app.revanced.patches.googlerecorder.restrictions

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.googlerecorder.restrictions.fingerprints.OnApplicationCreateFingerprint
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Remove device restrictions",
    description = "Removes restrictions from using the app on any device.",
    compatiblePackages = [CompatiblePackage("com.google.android.apps.recorder")]
)
@Suppress("unused")
object RemoveDeviceRestrictions : BytecodePatch(
    setOf(OnApplicationCreateFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        OnApplicationCreateFingerprint.result?.let {
            val featureStringIndex = it.scanResult.stringsScanResult!!.matches.first().index

            it.mutableMethod.apply {
                // Remove check for device restrictions.
                removeInstructions(featureStringIndex - 2, 5)

                val featureAvailableRegister = getInstruction<OneRegisterInstruction>(featureStringIndex).registerA

                // Override "isPixelDevice()" to return true.
                addInstruction(featureStringIndex, "const/4 v$featureAvailableRegister, 0x1")
            }
        } ?: throw OnApplicationCreateFingerprint.exception
    }
}
