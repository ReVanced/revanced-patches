package app.revanced.patches.protonvpn.delay

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused", "ObjectPropertyName")
val `Remove delay` by creatingBytecodePatch(
    description = "Removes the delay when changing servers."
) {
    compatibleWith("ch.protonvpn.android")

    apply {
        longDelayMethod.returnEarly(0)
        shortDelayMethod.returnEarly(0)
    }
}
