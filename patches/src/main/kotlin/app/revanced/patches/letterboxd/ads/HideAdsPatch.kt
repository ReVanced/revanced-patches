
package app.revanced.patches.letterboxd.ads

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val hideAdsPatch = bytecodePatch(
    name = "Hide ads",
) {
    compatibleWith("com.letterboxd.letterboxd")

    execute {
        admobHelperSetShowAdsFingerprint.method.addInstruction(0, "const p1, 0x0")
        listOf(admobHelperShouldShowAdsFingerprint, filmFragmentShowAdsFingerprint, memberExtensionShowAdsFingerprint).forEach {
            it.method.returnEarly()
        }
    }
}
