package app.revanced.patches.messenger.inputfield

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.messenger.inputfield.fingerprints.switchMessangeInputEmojiButtonFingerprint
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val disableSwitchingEmojiToStickerPatch = bytecodePatch(
    name = "Disable switching emoji to sticker",
    description = "Disables switching from emoji to sticker search mode in message input field.",
) {
    compatibleWith("com.facebook.orca")

    val switchMessangeInputEmojiButtonResult by switchMessangeInputEmojiButtonFingerprint

    execute {
        val setStringIndex = switchMessangeInputEmojiButtonResult.scanResult.patternScanResult!!.startIndex + 2

        switchMessangeInputEmojiButtonResult.mutableMethod.apply {
            val targetRegister = getInstruction<OneRegisterInstruction>(setStringIndex).registerA

            replaceInstruction(setStringIndex, "const-string v$targetRegister, \"expression\"")
        }
    }
}
