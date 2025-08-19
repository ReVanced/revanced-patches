package app.revanced.patches.backdrops.misc.pro

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
@Deprecated("This patch no longer works and will soon be deleted.")
val proUnlockPatch = bytecodePatch{
    compatibleWith("com.backdrops.wallpapers")

    execute {
        val registerIndex = proUnlockFingerprint.patternMatch!!.endIndex - 1

        proUnlockFingerprint.method.apply {
            val register = getInstruction<OneRegisterInstruction>(registerIndex).registerA
            addInstruction(
                proUnlockFingerprint.patternMatch!!.endIndex,
                "const/4 v$register, 0x1",
            )
        }
    }
}
