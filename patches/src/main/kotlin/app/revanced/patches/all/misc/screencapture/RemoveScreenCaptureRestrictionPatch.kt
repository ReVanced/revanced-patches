package app.revanced.patches.all.misc.screencapture

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.transformation.IMethodCall
import app.revanced.patches.all.misc.transformation.filterMapInstruction35c
import app.revanced.patches.all.misc.transformation.transformInstructionsPatch
import org.w3c.dom.Element

private val removeCaptureRestrictionResourcePatch = resourcePatch(
    description = "Sets allowAudioPlaybackCapture in manifest to true.",
) {
    execute {
        document("AndroidManifest.xml").use { document ->
            // Get the application node.
            val applicationNode =
                document
                    .getElementsByTagName("application")
                    .item(0) as Element

            // Set allowAudioPlaybackCapture attribute to true.
            applicationNode.setAttribute("android:allowAudioPlaybackCapture", "true")
        }
    }
}

private const val EXTENSION_CLASS_DESCRIPTOR_PREFIX =
    "Lapp/revanced/extension/all/misc/screencapture/removerestriction/RemoveScreenCaptureRestrictionPatch"
private const val EXTENSION_CLASS_DESCRIPTOR = "$EXTENSION_CLASS_DESCRIPTOR_PREFIX;"

@Suppress("unused")
val removeScreenCaptureRestrictionPatch = bytecodePatch(
    name = "Remove screen capture restriction",
    description = "Removes the restriction of capturing audio from apps that normally wouldn't allow it.",
    use = false,
) {
    extendWith("extensions/all/misc/screencapture/remove-screen-capture-restriction.rve")

    dependsOn(
        removeCaptureRestrictionResourcePatch,
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
    ),
}
