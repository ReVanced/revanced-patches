package app.revanced.patches.github.misc.gestures

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.github.misc.gestures.fingerprints.CodeViewFingerprint

@Patch(
    name = "Pinch to zoom",
    description = "Adds pinch to zoom functionality to the code viewer.",
    compatiblePackages = [CompatiblePackage("com.github.android")],
    requiresIntegrations = true
)
@Suppress("unused")
object PinchToZoomPatch : BytecodePatch(
    setOf(CodeViewFingerprint)
) {
    override fun execute(context: BytecodeContext) = CodeViewFingerprint.result?.mutableMethod?.addInstruction(
        0,
        "invoke-static { p2 }, Lapp/revanced/github/PinchToZoomPatch;->" +
                "addPinchToZoomGesture(Landroid/view/ViewGroup;)V"
    ) ?: throw CodeViewFingerprint.exception
}
