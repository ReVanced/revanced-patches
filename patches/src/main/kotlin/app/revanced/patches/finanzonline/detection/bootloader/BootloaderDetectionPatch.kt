package app.revanced.patches.finanzonline.detection.bootloader

import app.revanced.util.returnEarly
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused")
val `Remove bootloader detection` by creatingBytecodePatch(
    description = "Removes the check for an unlocked bootloader.",
) {
    compatibleWith("at.gv.bmf.bmf2go")

    apply {
        setOf(createKeyMethod, bootStateMethod).forEach { method ->
            method.returnEarly(true)
        }
    }
}
