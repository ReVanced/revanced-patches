package app.revanced.patches.willhaben.ads

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
internal val `Hide ads` by creatingBytecodePatch(
    description = "Hides all in-app ads.",
) {
    compatibleWith("at.willhaben")

    apply {
        adResolverMethod.returnEarly(null) // TODO
        whAdViewInjectorMethod.returnEarly()
    }
}
