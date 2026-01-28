package app.revanced.patches.willhaben.ads

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val hideAdsPatch = bytecodePatch(
    name = "Hide ads",
    description = "Hides all in-app ads.",
) {
    compatibleWith("at.willhaben")

    apply {
        adResolverMethod.returnEarly() // TODO
        whAdViewInjectorMethod.returnEarly()
    }
}
