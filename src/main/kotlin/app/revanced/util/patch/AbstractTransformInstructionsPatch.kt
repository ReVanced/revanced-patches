package app.revanced.util.patch

import app.revanced.extensions.findMutableMethodOf
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import org.jf.dexlib2.iface.ClassDef
import org.jf.dexlib2.iface.Method
import org.jf.dexlib2.iface.instruction.Instruction

internal abstract class AbstractTransformInstructionsPatch<T> : BytecodePatch() {

    abstract fun filterMap(
        classDef: ClassDef,
        method: Method,
        instruction: Instruction,
        instructionIndex: Int
    ): T?

    abstract fun transform(mutableMethod: MutableMethod, entry: T)

    override fun execute(context: BytecodeContext): PatchResult {
        // Find all instructions
        buildMap {
            context.classes.forEach { classDef ->
                classDef.methods.let { methods ->
                    buildMap methodList@{
                        methods.forEach methods@{ method ->
                            with(method.implementation?.instructions ?: return@methods) {
                                ArrayDeque<T>().also { patchIndices ->
                                    this.forEachIndexed { index, instruction ->
                                        val result = filterMap(classDef, method, instruction, index)
                                        if (result != null) {
                                            patchIndices.add(result)
                                        }
                                    }
                                }.also { if (it.isEmpty()) return@methods }.let { patches ->
                                    put(method, patches)
                                }
                            }
                        }
                    }
                }.also { if (it.isEmpty()) return@forEach }.let { methodPatches ->
                    put(classDef, methodPatches)
                }
            }
        }.forEach { (classDef, methods) ->
            // And finally transform the instructions...
            with(context.proxy(classDef).mutableClass) {
                methods.forEach { (method, patches) ->
                    val mutableMethod = findMutableMethodOf(method)
                    while (!patches.isEmpty()) {
                        transform(mutableMethod, patches.removeLast())
                    }
                }
            }
        }

        return PatchResultSuccess()
    }
}
