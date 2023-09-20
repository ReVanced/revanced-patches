package app.revanced.patches.idaustria.detection.root

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.idaustria.detection.root.fingerprints.RootDetectionFingerprint

@Patch(
    name = "Remove root detection",
    description = "Removes the check for root permissions and unlocked bootloader.",
    compatiblePackages = [CompatiblePackage("at.gv.oe.app")]
)
@Suppress("unused")
object RootDetectionPatch : BytecodePatch(
    setOf(RootDetectionFingerprint)
) {
    override fun execute(context: BytecodeContext) =
        RootDetectionFingerprint.result!!.mutableMethod.addInstruction(0, "return-void")
}
