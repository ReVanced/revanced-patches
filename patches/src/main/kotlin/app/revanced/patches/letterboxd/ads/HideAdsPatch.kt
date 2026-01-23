package app.revanced.patches.letterboxd.ads

import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val `Hide ads` by creatingBytecodePatch {
    compatibleWith("com.letterboxd.letterboxd")

    apply {
        admobHelperSetShowAdsMethod.addInstruction(0, "const p1, 0x0")
        listOf(admobHelperShouldShowAdsMethod, filmFragmentShowAdsMethod, memberExtensionShowAdsMethod).forEach {
            it.returnEarly(false)
        }
    }
}
