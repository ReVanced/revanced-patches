package app.revanced.patches.all.screenshot.removerestriction

import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.util.patch.AbstractTransformInstructionsPatch
import app.revanced.util.patch.IMethodCall
import app.revanced.util.patch.Instruction35cInfo
import app.revanced.util.patch.filterMapInstruction35c
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.Instruction

@Patch(
    name = "Remove screenshot restriction",
    description = "Removes the restriction of taking screenshots in apps that normally wouldn't allow it.",
    use = false,
    requiresIntegrations = true,
)
@Suppress("unused")
object RemoveScreenshotRestrictionPatch : AbstractTransformInstructionsPatch<Instruction35cInfo>() {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR_PREFIX =
            "Lapp/revanced/all/screenshot/removerestriction/RemoveScreenshotRestrictionPatch"
    private const val INTEGRATIONS_CLASS_DESCRIPTOR = "$INTEGRATIONS_CLASS_DESCRIPTOR_PREFIX;"

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
        SetFlags(
            "Landroid/view/Window;",
            "setFlags",
            arrayOf("I", "I"),
            "V",
        );
    }
}
