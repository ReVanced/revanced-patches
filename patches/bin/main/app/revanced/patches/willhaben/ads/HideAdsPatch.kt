package app.revanced.patches.willhaben.ads

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
internal val hideAdsPatch = bytecodePatch(
    name = "Hide ads",
    description = "Hides all in-app ads.",
) {
    compatibleWith("at.willhaben")

    execute {
        adResolverFingerprint.method.returnEarly()
        whAdViewInjectorFingerprint.method.returnEarly()
    }
}
