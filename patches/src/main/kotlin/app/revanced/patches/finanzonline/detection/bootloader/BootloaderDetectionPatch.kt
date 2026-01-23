package app.revanced.patches.finanzonline.detection.bootloader

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val bootloaderDetectionPatch = bytecodePatch(
    name = "Remove bootloader detection",
    description = "Removes the check for an unlocked bootloader.",
) {
    compatibleWith("at.gv.bmf.bmf2go")

    apply {
        setOf(createKeyMethod, bootStateMethod).forEach { method ->
            method.returnEarly(true)
        }
    }
}
