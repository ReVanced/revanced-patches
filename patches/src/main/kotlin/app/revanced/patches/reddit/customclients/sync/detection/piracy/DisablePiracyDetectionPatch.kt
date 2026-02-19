package app.revanced.patches.reddit.customclients.sync.detection.piracy

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

val disablePiracyDetectionPatch = bytecodePatch(
    description = "Disables detection of modified versions.",
) {

    apply {
        // Do not throw an error if the method can't be found.
        // This is fine because new versions of the target app do not need this patch.
        detectPiracyMethodOrNull?.returnEarly()
    }
}
