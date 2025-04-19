package app.revanced.patches.all.misc.screenshot

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.transformation.IMethodCall
import app.revanced.patches.all.misc.transformation.filterMapInstruction35c
import app.revanced.patches.all.misc.transformation.transformInstructionsPatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction22c
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

private const val EXTENSION_CLASS_DESCRIPTOR_PREFIX =
    "Lapp/revanced/extension/all/misc/screenshot/removerestriction/RemoveScreenshotRestrictionPatch"
private const val EXTENSION_CLASS_DESCRIPTOR = "$EXTENSION_CLASS_DESCRIPTOR_PREFIX;"

@Suppress("unused")
val removeScreenshotRestrictionPatch = bytecodePatch(
    name = "Remove screenshot restriction",
    description = "Removes the restriction of taking screenshots in apps that normally wouldn't allow it.",
    use = false,
) {
    extendWith("extensions/all/misc/screenshot/remove-screenshot-restriction.rve")

    dependsOn(
        // Remove the restriction of taking screenshots.
        transformInstructionsPatch(
            filterMap = { classDef, _, instruction, instructionIndex ->
                filterMapInstruction35c<MethodCall>(
                    EXTENSION_CLASS_DESCRIPTOR_PREFIX,
                    classDef,
                    instruction,
                    instructionIndex,
                )
            },
            transform = { mutableMethod, entry ->
                val (methodType, instruction, instructionIndex) = entry
                methodType.replaceInvokeVirtualWithExtension(
                    EXTENSION_CLASS_DESCRIPTOR,
                    mutableMethod,
                    instruction,
                    instructionIndex,
                )
            },
        ),
        // Modify layout params.
        transformInstructionsPatch(
            filterMap = { _, _, instruction, instructionIndex ->
                if (instruction.opcode != Opcode.IPUT) {
                    return@transformInstructionsPatch null
                }

                val instruction22c = instruction as Instruction22c
                val fieldReference = instruction22c.reference as FieldReference

                if (fieldReference.definingClass != "Landroid/view/WindowManager\$LayoutParams;" ||
                    fieldReference.name != "flags" ||
                    fieldReference.type != "I"
                ) {
                    return@transformInstructionsPatch null
                }

                Pair(instruction22c, instructionIndex)
            },
            transform = { mutableMethod, entry ->
                val (instruction, index) = entry
                val register = instruction.registerA

                mutableMethod.addInstructions(
                    index,
                    "and-int/lit16 v$register, v$register, -0x2001",
                )
            },
        ),
    )
}

// Information about method calls we want to replace
@Suppress("unused")
private enum class MethodCall(
    override val definedClassName: String,
    override val methodName: String,
    override val methodParams: Array<String>,
    override val returnType: String,
) : IMethodCall {
    AddFlags(
        "Landroid/view/Window;",
        "addFlags",
        arrayOf("I"),
        "V",
    ),
    SetFlags(
        "Landroid/view/Window;",
        "setFlags",
        arrayOf("I", "I"),
        "V",
    ),
}
