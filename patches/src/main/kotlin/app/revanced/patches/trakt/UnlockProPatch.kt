package app.revanced.patches.trakt

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.immutableClassDef
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused")
val `Unlock pro` by creatingBytecodePatch {
    compatibleWith("tv.trakt.trakt"("1.1.1"))

    apply {
        arrayOf(isVIPMethod, isVIPEPMethod).onEach { fingerprint ->
            // Resolve both fingerprints on the same class.
            fingerprint.match(remoteUserMethod.immutableClassDef) // TODO
        }.forEach { fingerprint ->
            // Return true for both VIP check methods.
            fingerprint.addInstructions(
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
