package app.revanced.patches.finanzonline.detection.root.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.finanzonline.detection.root.fingerprints.RootDetectionFingerprint
import app.revanced.patches.finanzonline.detection.shared.annotations.DetectionCompatibility

@Patch
@Name("Remove root detection")
@Description("Removes the check for root permissions.")
@DetectionCompatibility
class RootDetectionPatch : BytecodePatch(
    listOf(RootDetectionFingerprint)
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
