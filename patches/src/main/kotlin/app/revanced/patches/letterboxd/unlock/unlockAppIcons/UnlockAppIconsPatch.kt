package app.revanced.patches.letterboxd.unlock.unlockAppIcons

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val `Unlock app icons` by creatingBytecodePatch {
    compatibleWith("com.letterboxd.letterboxd")

    apply {
        getCanChangeAppIconMethod.returnEarly(true)
    }
}
