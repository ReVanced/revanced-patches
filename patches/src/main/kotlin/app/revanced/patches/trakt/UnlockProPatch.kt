package app.revanced.patches.trakt

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val unlockProPatch = bytecodePatch(
    name = "Unlock pro",
) {
    compatibleWith("tv.trakt.trakt"("1.1.1"))

    execute {
        arrayOf(isVIPFingerprint, isVIPEPFingerprint).onEach { fingerprint ->
            // Resolve both fingerprints on the same class.
            fingerprint.match(remoteUserFingerprint.originalClassDef)
        }.forEach { fingerprint ->
            // Return true for both VIP check methods.
            fingerprint.method.addInstructions(
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
