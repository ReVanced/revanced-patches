package app.revanced.patches.photomath.misc.unlock.bookpoint

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val enableBookpointPatch = bytecodePatch(
    description = "Enables textbook access",
) {

    apply {
        isBookpointEnabledMethod.returnEarly(true) // TODO: CHECK IF THIS IS FINE IN REPLACEMENT OF replaceInstructions
    }
}
