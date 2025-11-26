package app.revanced.patches.all.misc.transformation

import app.revanced.patcher.dex.mutable.MutableMethod
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.forEachInstructionAsSequence
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.Instruction

fun <T> transformInstructionsPatch(
    filterMap: (ClassDef, Method, Instruction, Int) -> T?,
    transform: (MutableMethod, T) -> Unit,
) = bytecodePatch {
    execute {
        forEachInstructionAsSequence { classDef, method, i, instruction ->
            transform(method, filterMap(classDef, method, instruction, i) ?: return@forEachInstructionAsSequence)
        }
    }
}
