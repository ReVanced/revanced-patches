package app.revanced.patches.all.misc.transformation

import app.revanced.com.android.tools.smali.dexlib2.mutable.MutableMethod
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.forEachInstructionAsSequence
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.Instruction

@Deprecated(
    "Use forEachInstructionAsSequence directly within a bytecodePatch", ReplaceWith(
        "bytecodePatch { apply { forEachInstructionAsSequence(filterMap, transform) } }",
        "app.revanced.util.forEachInstructionAsSequence",
        "app.revanced.patcher.patch.bytecodePatch",
    )
)
fun <T> transformInstructionsPatch(
    filterMap: (ClassDef, Method, Instruction, Int) -> T?,
    transform: (MutableMethod, T) -> Unit,
) = bytecodePatch {
    apply { forEachInstructionAsSequence(filterMap, transform) }
}
