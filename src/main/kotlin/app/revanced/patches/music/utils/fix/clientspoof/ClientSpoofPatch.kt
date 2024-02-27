package app.revanced.patches.music.utils.fix.clientspoof

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patches.music.utils.fix.clientspoof.fingerprints.UserAgentHeaderBuilderFingerprint
import app.revanced.patches.music.utils.microg.Constants.MUSIC_PACKAGE_NAME
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction

object ClientSpoofPatch : BytecodePatch(
    setOf(UserAgentHeaderBuilderFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        UserAgentHeaderBuilderFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = it.scanResult.patternScanResult!!.endIndex - 1
                val packageNameRegister =
                    getInstruction<FiveRegisterInstruction>(insertIndex).registerD

                addInstruction(
                    insertIndex,
                    "const-string v$packageNameRegister, \"$MUSIC_PACKAGE_NAME\""
                )
            }
        } ?: throw UserAgentHeaderBuilderFingerprint.exception

    }
}
