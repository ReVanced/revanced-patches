package app.revanced.patches.protonvpn.delay

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val removeDelayPatch = bytecodePatch(
    name = "Remove delay",
    description = "Removes the delay when changing servers.",
) {
    compatibleWith("ch.protonvpn.android")

    execute {
        longDelayFingerprint.method.returnEarly(0)
        shortDelayFingerprint.method.returnEarly(0)
    }
}