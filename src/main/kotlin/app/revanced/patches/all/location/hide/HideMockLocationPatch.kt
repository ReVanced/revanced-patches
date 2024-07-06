@file:Suppress("unused")

package app.revanced.patches.all.location.hide

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.all.misc.transformation.BaseTransformInstructionsPatch
import app.revanced.patches.all.misc.transformation.IMethodCall
import app.revanced.patches.all.misc.transformation.Instruction35cInfo
import app.revanced.patches.all.misc.transformation.filterMapInstruction35c
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.Instruction

@Patch(
    name = "Hide mock location",
    description = "Spoof the app when mocking the location.",
    use = false
)
object HideMockLocationPatch : BaseTransformInstructionsPatch<Instruction35cInfo>() {

    override fun filterMap(
        classDef: ClassDef,
        method: Method,
        instruction: Instruction,
        instructionIndex: Int
    ) = filterMapInstruction35c<MethodCall>(
        "",
        classDef,
        instruction,
        instructionIndex
    )

    override fun transform(mutableMethod: MutableMethod, entry: Instruction35cInfo) {
        val (_, instruction, instructionIndex) = entry
        mutableMethod.replaceInstruction(instructionIndex, "const/4 ${instruction.registerC}, 0x0")
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