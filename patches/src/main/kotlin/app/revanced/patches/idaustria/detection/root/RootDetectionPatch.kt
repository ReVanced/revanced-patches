package app.revanced.patches.idaustria.detection.root

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patches.idaustria.detection.deviceintegrity.`Remove device integrity checks`

@Deprecated("Patch was superseded", ReplaceWith("`Remove device integrity checks`"))
@Suppress("unused")
val rootDetectionPatch by creatingBytecodePatch {
    dependsOn(`Remove device integrity checks`)
}
