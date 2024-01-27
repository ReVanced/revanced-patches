package app.revanced.patches.all.screenshot.removerestriction

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.all.misc.transformation.BaseTransformInstructionsPatch
import app.revanced.patches.all.misc.transformation.IMethodCall
import app.revanced.patches.all.misc.transformation.Instruction35cInfo
import app.revanced.patches.all.misc.transformation.filterMapInstruction35c
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction22c
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

@Patch(
    name = "Remove screenshot restriction",
    description = "Removes the restriction of taking screenshots in apps that normally wouldn't allow it.",
    use = false,
    requiresIntegrations = true,
)
@Suppress("unused")
object RemoveScreenshotRestrictionPatch : BaseTransformInstructionsPatch<Instruction35cInfo>() {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR_PREFIX =
            "Lapp/revanced/integrations/all/screenshot/removerestriction/RemoveScreenshotRestrictionPatch"
    private const val INTEGRATIONS_CLASS_DESCRIPTOR = "$INTEGRATIONS_CLASS_DESCRIPTOR_PREFIX;"

    override fun execute(context: BytecodeContext) {
        super.execute(context)
        ModifyLayoutParamsFlags().execute(context)
    }

    override fun filterMap(
        classDef: ClassDef,
        method: Method,
        instruction: Instruction,
        instructionIndex: Int
    ) = filterMapInstruction35c<MethodCall>(
        INTEGRATIONS_CLASS_DESCRIPTOR_PREFIX,
        classDef,
        instruction,
        instructionIndex
    )

    override fun transform(mutableMethod: MutableMethod, entry: Instruction35cInfo) {
        val (methodType, instruction, instructionIndex) = entry
        methodType.replaceInvokeVirtualWithIntegrations(INTEGRATIONS_CLASS_DESCRIPTOR, mutableMethod, instruction, instructionIndex)
    }

    // Information about method calls we want to replace
    enum class MethodCall(
        override val definedClassName: String,
        override val methodName: String,
        override val methodParams: Array<String>,
        override val returnType: String
    ): IMethodCall {
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
        );
    }
}

private class ModifyLayoutParamsFlags : BaseTransformInstructionsPatch<Pair<Instruction22c, Int>>() {
    override fun filterMap(
        classDef: ClassDef,
        method: Method,
        instruction: Instruction,
        instructionIndex: Int
    ): Pair<Instruction22c, Int>? {
        if (instruction.opcode != Opcode.IPUT) {
            return null
        }

        val instruction22c = instruction as Instruction22c
        val fieldReference = instruction22c.reference as FieldReference

        if (fieldReference.definingClass != "Landroid/view/WindowManager\$LayoutParams;"
            || fieldReference.name != "flags"
            || fieldReference.type != "I") {
            return null
        }

        return Pair(instruction22c, instructionIndex)
    }

    override fun transform(mutableMethod: MutableMethod, entry: Pair<Instruction22c, Int>) {
        val (instruction, index) = entry
        val register = instruction.registerA

        mutableMethod.addInstructions(
            index,
            "and-int/lit16 v$register, v$register, -0x2001"
        )
    }
}
