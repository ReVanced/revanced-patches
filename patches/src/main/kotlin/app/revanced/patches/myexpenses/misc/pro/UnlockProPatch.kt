package app.revanced.patches.myexpenses.misc.pro

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val unlockProPatch = bytecodePatch("Unlock pro") {
    compatibleWith("org.totschnig.myexpenses"("3.4.9"))

    apply {
        isEnabledMethod.returnEarly(true)
    }
}
