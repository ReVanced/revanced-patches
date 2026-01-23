package app.revanced.patches.all.misc.connectivity.telephony.sim.spoof

import app.revanced.patcher.patch.bytecodePatch

@Deprecated("Patch was renamed", ReplaceWith("`Spoof SIM provider`"))
@Suppress("unused")
val spoofSimCountryPatch = bytecodePatch {
    dependsOn(`Spoof SIM provider`)
}