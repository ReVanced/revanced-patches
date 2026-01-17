package app.revanced.patches.myexpenses.misc.pro

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused", "ObjectPropertyName")
val `Unlock pro` by creatingBytecodePatch {
    compatibleWith("org.totschnig.myexpenses"("3.4.9"))

    apply {
        isEnabledMethod.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
    }
}
