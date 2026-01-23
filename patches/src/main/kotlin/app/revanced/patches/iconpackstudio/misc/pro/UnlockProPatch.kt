package app.revanced.patches.iconpackstudio.misc.pro

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val `Unlock pro` by creatingBytecodePatch {
    compatibleWith("ginlemon.iconpackstudio"("2.2 build 016"))

    apply {
        checkProMethod.returnEarly(true)
    }
}
