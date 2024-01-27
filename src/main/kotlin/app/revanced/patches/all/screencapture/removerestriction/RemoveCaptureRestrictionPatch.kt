package app.revanced.patches.all.screencapture.removerestriction

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
    name = "Remove screen capture restriction",
    description = "Removes the restriction of capturing audio from apps that normally wouldn't allow it.",
    dependencies = [RemoveCaptureRestrictionResourcePatch::class],
    use = false,
    requiresIntegrations = true
)
@Suppress("unused")
object RemoveCaptureRestrictionPatch : BaseTransformInstructionsPatch<Instruction35cInfo>() {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR_PREFIX =
        "Lapp/revanced/integrations/all/screencapture/removerestriction/RemoveScreencaptureRestrictionPatch"
    private const val INTEGRATIONS_CLASS_DESCRIPTOR = "$INTEGRATIONS_CLASS_DESCRIPTOR_PREFIX;"
    // Information about method calls we want to replace
    enum class MethodCall(
        override val definedClassName: String,
        override val methodName: String,
        override val methodParams: Array<String>,
        override val returnType: String
    ): IMethodCall {
        SetAllowedCapturePolicySingle(
            "Landroid/media/AudioAttributes\$Builder;",
            "setAllowedCapturePolicy",
            arrayOf("I"),
            "Landroid/media/AudioAttributes\$Builder;",
        ),
        SetAllowedCapturePolicyGlobal(
            "Landroid/media/AudioManager;",
            "setAllowedCapturePolicy",
            arrayOf("I"),
            "V",
        );
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
}
