package app.revanced.patches.trakt

import app.revanced.com.android.tools.smali.dexlib2.mutable.MutableMethod
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val unlockProPatch = bytecodePatch("Unlock pro") {
    compatibleWith("tv.trakt.trakt"("1.1.1"))

    apply {
        // Return true for both VIP check methods.
        arrayOf(isVIPMethod, isVIPEPMethod).forEach { method: MutableMethod ->
            method.addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    invoke-static {v0}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
                    move-result-object v1
                    return-object v1
                """,
            )
        }
    }
}
