package app.revanced.patches.idaustria.detection.root

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.shared.PATCH_DESCRIPTION_REMOVE_ROOT_DETECTION
import app.revanced.patches.shared.PATCH_NAME_REMOVE_ROOT_DETECTION
import app.revanced.util.returnEarly

@Suppress("unused")
val rootDetectionPatch = bytecodePatch(
    name = PATCH_NAME_REMOVE_ROOT_DETECTION,
    description = PATCH_DESCRIPTION_REMOVE_ROOT_DETECTION
) {
    compatibleWith("at.gv.oe.app")

    execute {
        setOf(
            attestationSupportedCheckFingerprint,
            bootloaderCheckFingerprint,
            rootCheckFingerprint,
        ).forEach { it.method.returnEarly(true) }
    }
}
