package app.revanced.patches.all.misc.adb

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.transformation.transformInstructionsPatch
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference
import com.android.tools.smali.dexlib2.util.MethodUtil

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/all/misc/hide/adb/HideAdbPatch;"

private val SETTINGS_GLOBAL_GET_INT_OR_THROW_METHOD_REFERENCE = ImmutableMethodReference(
    "Landroid/provider/Settings\$Global;",
    "getInt",
    listOf("Landroid/content/ContentResolver;", "Ljava/lang/String;"),
    "I"
)

private val SETTINGS_GLOBAL_GET_INT_OR_DEFAULT_METHOD_REFERENCE = ImmutableMethodReference(
    "Landroid/provider/Settings\$Global;",
    "getInt",
    listOf("Landroid/content/ContentResolver;", "Ljava/lang/String;", "I"),
    "I"
)

private fun MethodReference.anyMethodSignatureMatches(vararg anyOf: MethodReference): Boolean {
    return anyOf.any {
        MethodUtil.methodSignaturesMatch(it, this)
    }
}

@Suppress("unused")
val hideAdbStatusPatch = bytecodePatch(
    name = "Hide ADB status",
    description = "Hides enabled development settings and/or ADB.",
    use = false,
) {
    extendWith("extensions/all/misc/adb/hide-adb.rve")

    dependsOn(
        transformInstructionsPatch(
            filterMap = filterMap@{ classDef, method, instruction, instructionIndex ->
                val reference = instruction
                    .takeIf { it.opcode == Opcode.INVOKE_STATIC }
                    ?.getReference<MethodReference>()
                    ?.takeIf {
                        it.anyMethodSignatureMatches(
                            SETTINGS_GLOBAL_GET_INT_OR_THROW_METHOD_REFERENCE,
                            SETTINGS_GLOBAL_GET_INT_OR_DEFAULT_METHOD_REFERENCE
                        )
                    }
                    ?: return@filterMap null

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
