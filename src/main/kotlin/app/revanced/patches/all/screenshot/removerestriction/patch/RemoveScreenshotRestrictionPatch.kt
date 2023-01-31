package app.revanced.patches.all.screenshot.removerestriction.patch

import app.revanced.extensions.findMutableMethodOf
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.formats.Instruction35c
import org.jf.dexlib2.iface.reference.MethodReference

@Patch(false)
@Name("remove-screenshot-restriction")
@Description("Removes the restriction of taking screenshots in apps that normally wouldn't allow it.")
@Version("0.0.1")
class RemoveScreenshotRestrictionPatch : BytecodePatch() {

    private companion object {
        const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/all/screenshot/removerestriction/RemoveScreenshotRestrictionPatch;"
    }

    // Information about method calls we want to replace
    private enum class MethodCall(
        val definedClassName: String,
        val methodName: String,
        val replacementMethodDefinition: String
    ) {
        SetFlags(
            "Landroid/view/Window;",
            "setFlags",
            "setFlags(Landroid/view/Window;II)V",
        );

        fun replaceInstruction(method: MutableMethod, instruction: Instruction35c, instructionIndex: Int) {
            when (this) {
                SetFlags -> {
                    method.replaceInstruction(
                        instructionIndex,
                        "invoke-static { v${instruction.registerC}, v${instruction.registerD}, v${instruction.registerE} }, ${INTEGRATIONS_CLASS_DESCRIPTOR}->${replacementMethodDefinition}"
                    )
                }
            }
        }

        companion object {
            fun fromMethodReference(methodReference: MethodReference) = values().firstOrNull { search ->
                search.definedClassName == methodReference.definingClass && search.methodName == methodReference.name
            }
        }
    }

    override fun execute(context: BytecodeContext): PatchResult {
        // Find all instructions where one of the methods is called
        buildMap {
            context.classes.forEach { classDef ->
                if (classDef.type == INTEGRATIONS_CLASS_DESCRIPTOR) {
                    // avoid infinite recursion
                    return@forEach
                }

                classDef.methods.let { methods ->
                    buildMap methodList@{
                        methods.forEach methods@{ method ->
                            with(method.implementation?.instructions ?: return@methods) {
                                ArrayDeque<Triple<MethodCall, Instruction35c, Int>>().also { patchIndices ->
                                    this.forEachIndexed { index, instruction ->
                                        if (instruction.opcode != Opcode.INVOKE_VIRTUAL) return@forEachIndexed

                                        val invokeInstruction = instruction as Instruction35c
                                        val methodRef = invokeInstruction.reference as MethodReference
                                        val methodCall = MethodCall.fromMethodReference(methodRef) ?: return@forEachIndexed

                                        patchIndices.add(Triple(methodCall, invokeInstruction, index))
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
            // And finally replace the instructions...
            with(context.proxy(classDef).mutableClass) {
                methods.forEach { (method, patches) ->
                    val mutableMethod = findMutableMethodOf(method)
                    while (!patches.isEmpty()) {
                        val (methodType, instruction, instructionIndex) = patches.removeLast()
                        methodType.replaceInstruction(mutableMethod, instruction, instructionIndex)
                    }
                }
            }
        }

        return PatchResultSuccess()
    }
}
