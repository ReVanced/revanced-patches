package app.revanced.patches.myexpenses.misc.pro

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused", "ObjectPropertyName")
val `Unlock pro` by creatingBytecodePatch {
    compatibleWith("org.totschnig.myexpenses"("3.4.9"))

    apply {
        isEnabledMethod.returnEarly(true)
    }
}
