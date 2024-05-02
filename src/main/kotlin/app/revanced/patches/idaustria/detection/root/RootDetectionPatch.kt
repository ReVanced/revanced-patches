package app.revanced.patches.idaustria.detection.root

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.idaustria.detection.root.fingerprints.AttestationSupportedCheckFingerprint
import app.revanced.patches.idaustria.detection.root.fingerprints.BootloaderCheckFingerprint
import app.revanced.patches.idaustria.detection.root.fingerprints.RootCheckFingerprint
import app.revanced.util.returnEarly

@Patch(
    name = "Remove root detection",
    description = "Removes the check for root permissions and unlocked bootloader.",
    compatiblePackages = [CompatiblePackage("at.gv.oe.app")]
)
@Suppress("unused")
object RootDetectionPatch : BytecodePatch(
    setOf(AttestationSupportedCheckFingerprint, BootloaderCheckFingerprint, RootCheckFingerprint)
) {
    override fun execute(context: BytecodeContext) = listOf(
        AttestationSupportedCheckFingerprint,
        BootloaderCheckFingerprint,
        RootCheckFingerprint
    ).returnEarly(true)
}
