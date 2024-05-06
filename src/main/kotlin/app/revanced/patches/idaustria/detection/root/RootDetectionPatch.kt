package app.revanced.patches.idaustria.detection.root

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.idaustria.detection.root.fingerprints.attestationSupportedCheckFingerprint
import app.revanced.patches.idaustria.detection.root.fingerprints.bootloaderCheckFingerprint
import app.revanced.patches.idaustria.detection.root.fingerprints.rootCheckFingerprint
import app.revanced.util.returnEarly

@Suppress("unused")
val rootDetectionPatch = bytecodePatch(
    name = "Root detection",
    description = "Removes the check for root permissions and unlocked bootloader."
) {
    compatibleWith("at.gv.oe.app"())

    attestationSupportedCheckFingerprint()
    bootloaderCheckFingerprint()
    rootCheckFingerprint()

    execute {
        listOf(
            attestationSupportedCheckFingerprint,
            bootloaderCheckFingerprint,
            rootCheckFingerprint
        ).returnEarly(true)
    }
}
