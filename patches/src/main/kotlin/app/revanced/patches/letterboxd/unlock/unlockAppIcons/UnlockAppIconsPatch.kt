package app.revanced.patches.letterboxd.unlock.unlockAppIcons

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused", "ObjectPropertyName")
val `Unlock app icons` by creatingBytecodePatch {
    compatibleWith("com.letterboxd.letterboxd")

    apply {
        getCanChangeAppIconMethod.returnEarly(true)
    }
}
