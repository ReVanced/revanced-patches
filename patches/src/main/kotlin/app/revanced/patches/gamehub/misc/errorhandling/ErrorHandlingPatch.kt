package app.revanced.patches.gamehub.misc.errorhandling

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.Opcode

internal val errorHandlingPatch = bytecodePatch {
    execute {
        // NetErrorHandler$DefaultImpls — insert return-void before goto/16 so error callbacks
        // are silenced. The goto jumps to a logging/dialog path; skipping it avoids error popups.
        netErrorHandlerFingerprint.method.apply {
            val gotoIndex = indexOfFirstInstructionOrThrow { opcode == Opcode.GOTO_16 }
            addInstruction(gotoIndex, "return-void")
        }

        // TipUtils.c(String) — suppress tip/toast dialogs entirely.
        tipUtilsFingerprint.method.returnEarly()
    }
}
