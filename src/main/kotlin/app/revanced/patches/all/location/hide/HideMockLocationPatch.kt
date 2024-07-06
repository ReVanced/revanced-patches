@file:Suppress("unused")

package app.revanced.patches.all.location.hide

import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.all.misc.transformation.BaseTransformInstructionsPatch
import app.revanced.patches.all.misc.transformation.IMethodCall
import app.revanced.patches.all.misc.transformation.Instruction35cInfo
import app.revanced.patches.all.misc.transformation.fromMethodReference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "Hide mock location",
    description = "Prevents the app from knowing the device location is being mocked by a third party app.",
    use = false
)
object HideMockLocationPatch : BaseTransformInstructionsPatch<Instruction35cInfo>() {

    override fun filterMap(
        classDef: ClassDef,
        method: Method,
        instruction: Instruction,
        instructionIndex: Int
    ): Instruction35cInfo? {
        if (instruction.opcode != Opcode.INVOKE_VIRTUAL) {
            return null
        }

        val invokeInstruction = instruction as Instruction35c
        val methodRef = invokeInstruction.reference as MethodReference
        val methodCall = fromMethodReference<MethodCall>(methodRef) ?: return null

        return Instruction35cInfo(methodCall, invokeInstruction, instructionIndex)
    }

    override fun transform(mutableMethod: MutableMethod, entry: Instruction35cInfo) {
        val (_, instruction, instructionIndex) = entry

        //Replace invocation with a constant `false` boolean
        mutableMethod.replaceInstruction(instructionIndex, "const/4 v${instruction.registerC}, 0x0")
        //Remove "move-result" instruction
        mutableMethod.removeInstruction(instructionIndex + 1)
    }
}

private enum class MethodCall(
    override val definedClassName: String,
    override val methodName: String,
    override val methodParams: Array<String>,
    override val returnType: String
) : IMethodCall {
    IsMock("Landroid/location/Location;", "isMock", emptyArray(), "Z"),
    IsFromMockProvider("Landroid/location/Location;", "isFromMockProvider", emptyArray(), "Z")
}