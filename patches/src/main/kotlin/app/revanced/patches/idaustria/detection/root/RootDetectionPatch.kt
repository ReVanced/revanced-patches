package app.revanced.patches.idaustria.detection.root

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.idaustria.detection.deviceintegrity.removeDeviceIntegrityChecksPatch

@Deprecated("Patch was superseded", ReplaceWith("removeDeviceIntegrityChecksPatch"))
@Suppress("unused")
val rootDetectionPatch = bytecodePatch {
    dependsOn(removeDeviceIntegrityChecksPatch)
}
