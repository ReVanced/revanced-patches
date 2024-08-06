@file:Suppress("unused")

package app.revanced.patches.all.location.hide

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.all.misc.transformation.BaseTransformInstructionsPatch
import app.revanced.patches.all.misc.transformation.IMethodCall
import app.revanced.patches.all.misc.transformation.fromMethodReference
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "Hide mock location",
    description = "Prevents the app from knowing the device location is being mocked by a third party app.",
    use = false
)
object HideMockLocationPatch : BaseTransformInstructionsPatch<Pair<Instruction, Int>>() {
    override fun filterMap(
        classDef: ClassDef,
        method: Method,
        instruction: Instruction,
        instructionIndex: Int
    ): Pair<Instruction, Int>? {
        val reference = instruction.getReference<MethodReference>() ?: return null
        if (fromMethodReference<MethodCall>(reference) == null) return null

        return instruction to instructionIndex
    }

    override fun transform(mutableMethod: MutableMethod, entry: Pair<Instruction, Int>) {
        val (instruction, index) = entry
        instruction as FiveRegisterInstruction

        // Replace return value with a constant `false` boolean.
        mutableMethod.replaceInstruction(
            index + 1,
            "const/4 v${instruction.registerC}, 0x0"
        )
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
