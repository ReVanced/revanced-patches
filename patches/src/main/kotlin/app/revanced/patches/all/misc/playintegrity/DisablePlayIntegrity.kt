package app.revanced.patches.all.misc.playintegrity

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.transformation.transformInstructionsPatch
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference
import com.android.tools.smali.dexlib2.util.MethodUtil

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/playintegrity/DisablePlayIntegrityPatch;"

private val CONTEXT_BIND_SERVICE_METHOD_REFERENCE = ImmutableMethodReference(
    "Landroid/content/Context;",
    "bindService",
    listOf("Landroid/content/Intent;", "Landroid/content/ServiceConnection;", "I"),
    "Z"
)


@Suppress("unused")
val disablePlayIntegrityPatch = bytecodePatch(
    name = "Disable Play Integrity",
    description = "Prevents apps from using Play Integrity by pretending it is not available.",
    use = false,
) {
    extendWith("extensions/all/misc/disable-play-integrity.rve")

    dependsOn(
        transformInstructionsPatch(
            filterMap = filterMap@{ classDef, method, instruction, instructionIndex ->
                val reference = instruction
                    .getReference<MethodReference>()
                    ?.takeIf {
                        MethodUtil.methodSignaturesMatch(CONTEXT_BIND_SERVICE_METHOD_REFERENCE, it)
                    }
                    ?: return@filterMap null

                Triple(instruction as Instruction35c, instructionIndex, reference.parameterTypes)
            },
            transform = { method, entry ->
                val (instruction, index, parameterTypes) = entry
                val parameterString = parameterTypes.joinToString(separator = "")
                val registerString = "v${instruction.registerC}, v${instruction.registerD}, v${instruction.registerE}, v${instruction.registerF}"

                method.replaceInstruction(
                    index,
                    "invoke-static { $registerString }, $EXTENSION_CLASS_DESCRIPTOR->bindService(Landroid/content/Context;$parameterString)Z"
                )
            }
        )
    )
}
