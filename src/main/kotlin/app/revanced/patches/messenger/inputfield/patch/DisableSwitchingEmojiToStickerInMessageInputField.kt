package app.revanced.patches.messenger.inputfield.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.*
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.messenger.inputfield.fingerprints.SwitchMessangeInputEmojiButtonFingerprint
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction

@Patch
@Name("disable-switching-emoji-to-sticker-in-message-input-field")
@Description("Disables switching from emoji to sticker search mode in message input field")
@Compatibility([Package("com.facebook.orca")])
@Version("0.0.1")
class DisableSwitchingEmojiToStickerInMessageInputField : BytecodePatch(listOf(SwitchMessangeInputEmojiButtonFingerprint)) {
    override fun execute(context: BytecodeContext): PatchResult {
        SwitchMessangeInputEmojiButtonFingerprint.result?.let {
            val setStringIndex = it.scanResult.patternScanResult!!.startIndex + 2

            it.mutableMethod.apply {
                val targetRegister = getInstruction<OneRegisterInstruction>(setStringIndex).registerA

                replaceInstruction(
                    setStringIndex,
                    "const-string v$targetRegister, \"expression\""
                )
            }
        } ?: throw SwitchMessangeInputEmojiButtonFingerprint.toErrorResult()

        return PatchResultSuccess()
    }
}
