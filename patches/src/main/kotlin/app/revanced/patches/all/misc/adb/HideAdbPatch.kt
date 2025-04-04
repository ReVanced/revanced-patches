package app.revanced.patches.all.misc.adb

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.transformation.transformInstructionsPatch
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/all/spoof/adb/SpoofAdbPatch;"
private const val SETTINGS_GLOBAL_CLASS_DESCRIPTOR = "Landroid/provider/Settings\$Global;"

@Suppress("unused")
val hideAdbStatusPatch = bytecodePatch(
    name = "Hide ADB status",
    description = "Hide enabled development settings and/or ADB",
    use = false,
) {
    extendWith("extensions/all/misc/adb/hide-adb.rve")

    dependsOn(
        transformInstructionsPatch(
            filterMap = filterMap@{ classDef, method, instruction, instructionIndex ->
                if (instruction.opcode != Opcode.INVOKE_STATIC) return@filterMap null

                val reference = instruction.getReference<MethodReference>() ?: return@filterMap null

                if (reference.definingClass != SETTINGS_GLOBAL_CLASS_DESCRIPTOR
                    || reference.name != "getInt"
                    || reference.returnType != "I"
                ) return@filterMap null

                Triple(instruction as Instruction35c, instructionIndex, reference.parameterTypes)
            },
            transform = { method, entry ->
                val (instruction, index, parameterTypes) = entry
                val parameterString = parameterTypes.joinToString(separator = "")

                val registerString = when (parameterTypes.size) {
                    2 -> "v${instruction.registerC}, v${instruction.registerD}"
                    else -> "v${instruction.registerC}, v${instruction.registerD}, v${instruction.registerE}"
                }

                method.replaceInstruction(
                    index,
                    "invoke-static { $registerString }, $EXTENSION_CLASS_DESCRIPTOR->getInt($parameterString)I"
                )
            }
        )
    )
}
