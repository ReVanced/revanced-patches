package app.revanced.patches.youtube.misc.fix.playback

import app.revanced.util.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.misc.fix.playback.fingerprints.UserAgentHeaderBuilderFingerprint
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction

@Patch(
    name = "Client spoof",
    description = "Adds options to spoof the client to allow video playback.",
    dependencies = [SpoofSignaturePatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube", [
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39",
                "19.03.36",
                "19.04.38",
                "19.05.36",
                "19.06.39",
                "19.07.40",
                "19.08.36",
                "19.09.37"
            ]
        )
    ]
)
object ClientSpoofPatch : BytecodePatch(
    setOf(UserAgentHeaderBuilderFingerprint)
) {
    private const val ORIGINAL_PACKAGE_NAME = "com.google.android.youtube"

    override fun execute(context: BytecodeContext) {
        UserAgentHeaderBuilderFingerprint.result?.let { result ->
            val insertIndex = result.scanResult.patternScanResult!!.endIndex
           result.mutableMethod.apply {
               val packageNameRegister = getInstruction<FiveRegisterInstruction>(insertIndex).registerD

               addInstruction(insertIndex, "const-string v$packageNameRegister, \"$ORIGINAL_PACKAGE_NAME\"")
           }

        } ?: throw UserAgentHeaderBuilderFingerprint.exception
    }
}
