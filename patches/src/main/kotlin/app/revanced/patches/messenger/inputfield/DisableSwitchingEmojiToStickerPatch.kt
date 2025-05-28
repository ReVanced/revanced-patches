package app.revanced.patches.messenger.inputfield

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

/**
 * This patch will be deleted soon.
 *
 * Pull requests to update this patch to the latest app target are invited.
 */
@Deprecated("This patch only works with an outdated app target that is no longer fully supported by Facebook.")
@Suppress("unused")
val disableSwitchingEmojiToStickerPatch = bytecodePatch(
    description = "Disables switching from emoji to sticker search mode in message input field.",
) {
    compatibleWith("com.facebook.orca"("439.0.0.29.119"))

    execute {
        switchMessengeInputEmojiButtonFingerprint.method.apply {
            val setStringIndex = switchMessengeInputEmojiButtonFingerprint.patternMatch!!.startIndex + 2
            val targetRegister = getInstruction<OneRegisterInstruction>(setStringIndex).registerA

            replaceInstruction(setStringIndex, "const-string v$targetRegister, \"expression\"")
        }
    }
}
