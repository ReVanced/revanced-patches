package app.revanced.patches.youtube.utils.returnyoutubedislike.rollingnumber.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

/**
 * Compatible with YouTube v18.30.xx to v18.49.xx
 */
object RollingNumberMeasureAnimatedTextFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.ADD_FLOAT_2ADDR, // measuredTextWidth
        Opcode.ADD_INT_LIT8,
        Opcode.GOTO
    ),
    customFingerprint = custom@{ methodDef, _ ->
        if (methodDef.implementation == null)
            return@custom false

        for (instruction in methodDef.implementation!!.instructions) {
            if (instruction.opcode != Opcode.INVOKE_VIRTUAL)
                continue

            val invokeInstruction = instruction as ReferenceInstruction
            if (!invokeInstruction.reference.toString().endsWith("Landroid/text/TextPaint;->measureText([CII)F"))
                continue

            return@custom true
        }
        return@custom false
    }
)