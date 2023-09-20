package app.revanced.patches.finanzonline.detection.root

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.finanzonline.detection.root.fingerprints.RootDetectionFingerprint

@Patch(
    name = "Remove root detection",
    description = "Removes the check for root permissions.",
    compatiblePackages = [CompatiblePackage("at.gv.bmf.bmf2go")]
)
@Suppress("unused")
object RootDetectionPatch : BytecodePatch(
    setOf(RootDetectionFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        RootDetectionFingerprint.result?.mutableMethod?.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                return-object v0
            """
        ) ?: throw RootDetectionFingerprint.exception
    }
}
