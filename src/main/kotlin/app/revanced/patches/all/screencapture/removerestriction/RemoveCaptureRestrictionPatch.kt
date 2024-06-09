package app.revanced.patches.all.screencapture.removerestriction

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.transformation.IMethodCall
import app.revanced.patches.all.misc.transformation.filterMapInstruction35c
import app.revanced.patches.all.misc.transformation.transformInstructionsPatch

private const val INTEGRATIONS_CLASS_DESCRIPTOR_PREFIX =
    "Lapp/revanced/integrations/all/screencapture/removerestriction/RemoveScreencaptureRestrictionPatch"
private const val INTEGRATIONS_CLASS_DESCRIPTOR = "$INTEGRATIONS_CLASS_DESCRIPTOR_PREFIX;"

@Suppress("unused")
val removeCaptureRestrictionPatch = bytecodePatch(
    name = "Remove screen capture restriction",
    description = "Removes the restriction of capturing audio from apps that normally wouldn't allow it.",
    use = false,
    requiresIntegrations = true
) {
    dependsOn(
        removeCaptureRestrictionResourcePatch,
        transformInstructionsPatch(
            filterMap = { classDef, method, instruction, instructionIndex ->
                filterMapInstruction35c<MethodCall>(
                    INTEGRATIONS_CLASS_DESCRIPTOR_PREFIX,
                    classDef,
                    instruction,
                    instructionIndex
                )
            },
            transform = { mutableMethod, entry ->
                val (methodType, instruction, instructionIndex) = entry
                methodType.replaceInvokeVirtualWithIntegrations(
                    INTEGRATIONS_CLASS_DESCRIPTOR,
                    mutableMethod,
                    instruction,
                    instructionIndex
                )
            }
        )
    )
}

// Information about method calls we want to replace
enum class MethodCall(
    override val definedClassName: String,
    override val methodName: String,
    override val methodParams: Array<String>,
    override val returnType: String
) : IMethodCall {
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