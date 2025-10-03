package app.revanced.patches.serviceportalbund.detection.root

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.shared.PATCH_DESCRIPTION_REMOVE_ROOT_DETECTION
import app.revanced.patches.shared.PATCH_NAME_REMOVE_ROOT_DETECTION

@Suppress("unused")
val rootDetectionPatch = bytecodePatch(
    name = PATCH_NAME_REMOVE_ROOT_DETECTION,
    description = PATCH_DESCRIPTION_REMOVE_ROOT_DETECTION
) {
    compatibleWith("at.gv.bka.serviceportal")

    execute {
        rootDetectionFingerprint.method.addInstruction(0, "return-void")
    }
}
