package app.revanced.patches.messenger.inputfield

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val disableSwitchingEmojiToStickerPatch = bytecodePatch(
    name = "Disable switching emoji to sticker",
    description = "Disables switching from emoji to sticker search mode in message input field.",
) {
    compatibleWith("com.facebook.orca")

    val switchMessangeInputEmojiButtonFingerprintResult by switchMessangeInputEmojiButtonFingerprint

    execute {
        val setStringIndex = switchMessangeInputEmojiButtonFingerprintResult.scanResult.patternScanResult!!.startIndex + 2

        switchMessangeInputEmojiButtonFingerprintResult.mutableMethod.apply {
            val targetRegister = getInstruction<OneRegisterInstruction>(setStringIndex).registerA

            replaceInstruction(setStringIndex, "const-string v$targetRegister, \"expression\"")
        }
    }
}
