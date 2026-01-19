package app.revanced.patches.instagram.shared

import app.revanced.patcher.Fingerprint
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

context(BytecodePatchContext)
internal fun Fingerprint.replaceStringWithBogus(
    targetString: String,
) {
    val targetStringIndex = stringMatches!!.first { match -> match.string == targetString }.index
    val targetStringRegister = method.getInstruction<OneRegisterInstruction>(targetStringIndex).registerA

    /**
     * Replaces the 'target string' with 'BOGUS'.
     * This is usually done when we need to override a JSON key or url,
     * to skip with a random string that is not a valid JSON key.
     */
    method.replaceInstruction(
        targetStringIndex,
        "const-string v$targetStringRegister, \"BOGUS\"",
    )
}