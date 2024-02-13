package app.revanced.patches.all.misc.transformation

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.util.findMutableMethodOf
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.Instruction

@Suppress("MemberVisibilityCanBePrivate")
abstract class BaseTransformInstructionsPatch<T> : BytecodePatch(emptySet()) {
    abstract fun filterMap(
        classDef: ClassDef,
        method: Method,
        instruction: Instruction,
        instructionIndex: Int,
    ): T?

    abstract fun transform(mutableMethod: MutableMethod, entry: T)

    // Returns the patch indices as a Sequence, which will execute lazily.
    fun findPatchIndices(classDef: ClassDef, method: Method): Sequence<T>? {
        return method.implementation?.instructions?.asSequence()?.withIndex()?.mapNotNull { (index, instruction) ->
            filterMap(classDef, method, instruction, index)
        }
    }

    override fun execute(context: BytecodeContext) {
        // Find all methods to patch
        buildMap {
            context.classes.forEach { classDef ->
                val methods = buildList {
                    classDef.methods.forEach { method ->
                        // Since the Sequence executes lazily,
                        // using any() results in only calling
                        // filterMap until the first index has been found.
                        if (findPatchIndices(classDef, method)?.any() == true) add(method)
                    }
                }

                if (methods.isNotEmpty()) {
                    put(classDef, methods)
                }
            }
        }.forEach { (classDef, methods) ->
            // And finally transform the methods...
            val mutableClass = context.proxy(classDef).mutableClass

            methods.map(mutableClass::findMutableMethodOf).forEach methods@{ mutableMethod ->
                val patchIndices = findPatchIndices(mutableClass, mutableMethod)?.toCollection(ArrayDeque())
                    ?: return@methods

                while (!patchIndices.isEmpty()) transform(mutableMethod, patchIndices.removeLast())
            }
        }
    }
}
