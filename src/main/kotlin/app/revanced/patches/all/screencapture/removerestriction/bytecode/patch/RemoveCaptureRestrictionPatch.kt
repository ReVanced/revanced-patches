package app.revanced.patches.all.screencapture.removerestriction.bytecode.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.annotations.RequiresIntegrations
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.all.screencapture.removerestriction.resource.patch.RemoveCaptureRestrictionResourcePatch
import app.revanced.util.patch.AbstractTransformInstructionsPatch
import app.revanced.util.patch.IMethodCall
import app.revanced.util.patch.Instruction35cInfo
import app.revanced.util.patch.filterMapInstruction35c
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.Instruction

@Patch(false)
@Name("Remove screen capture restriction")
@Description("Removes the restriction of capturing audio from apps that normally wouldn't allow it.")
@DependsOn([RemoveCaptureRestrictionResourcePatch::class])
@RequiresIntegrations
class RemoveCaptureRestrictionPatch : AbstractTransformInstructionsPatch<Instruction35cInfo>() {
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

    private companion object {
        const val INTEGRATIONS_CLASS_DESCRIPTOR_PREFIX =
            "Lapp/revanced/all/screencapture/removerestriction/RemoveScreencaptureRestrictionPatch"
        const val INTEGRATIONS_CLASS_DESCRIPTOR = "$INTEGRATIONS_CLASS_DESCRIPTOR_PREFIX;"
    }
}
