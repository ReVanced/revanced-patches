package app.revanced.patches.idaustria.detection.root

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val rootDetectionPatch = bytecodePatch(
    name = "Remove root detection",
    description = "Removes the check for root permissions and unlocked bootloader.",
) {
    compatibleWith("at.gv.oe.app")

    attestationSupportedCheckFingerprint()
    bootloaderCheckFingerprint()
    rootCheckFingerprint()

    execute {
        setOf(attestationSupportedCheckFingerprint, bootloaderCheckFingerprint, rootCheckFingerprint).returnEarly(true)
    }
}
