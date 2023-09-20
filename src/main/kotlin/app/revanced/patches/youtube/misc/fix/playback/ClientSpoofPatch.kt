package app.revanced.patches.youtube.misc.fix.playback

import app.revanced.extensions.exception
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
    description = "Spoofs the client to allow playback.",
    dependencies = [SpoofSignatureVerificationPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.16.37",
                "18.19.35",
                "18.20.39",
                "18.23.35",
                "18.29.38",
                "18.32.39"
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
