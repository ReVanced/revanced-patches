package app.revanced.patches.music.flyoutpanel.utils

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

internal object EnumUtils {

    fun MutableMethod.getEnumIndex(): Int {
        var targetIndex = 0
        for ((index, instruction) in implementation!!.instructions.withIndex()) {
            if (instruction.opcode != Opcode.INVOKE_STATIC) continue

            val targetParameter = getInstruction<ReferenceInstruction>(index).reference

            if (!targetParameter.toString().contains("(I)")) continue

            targetIndex = index + 1
            break
        }
        if (targetIndex == 0)
            throw PatchException("Target reference not found!")

        return targetIndex
    }
}