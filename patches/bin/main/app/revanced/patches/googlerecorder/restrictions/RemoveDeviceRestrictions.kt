package app.revanced.patches.googlerecorder.restrictions

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val removeDeviceRestrictionsPatch = bytecodePatch(
    name = "Remove device restrictions",
    description = "Removes restrictions from using the app on any device. Requires mounting patched app over original.",
) {
    compatibleWith("com.google.android.apps.recorder")

    execute {
        val featureStringIndex = onApplicationCreateFingerprint.stringMatches!!.first().index

        onApplicationCreateFingerprint.method.apply {
            // Remove check for device restrictions.
            removeInstructions(featureStringIndex - 2, 5)

            val featureAvailableRegister = getInstruction<OneRegisterInstruction>(featureStringIndex).registerA

            // Override "isPixelDevice()" to return true.
            addInstruction(featureStringIndex, "const/4 v$featureAvailableRegister, 0x1")
        }
    }
}
