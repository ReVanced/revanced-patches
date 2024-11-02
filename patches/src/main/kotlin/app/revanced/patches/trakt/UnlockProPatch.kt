package app.revanced.patches.trakt

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.exception
import app.revanced.util.matchOrThrow

@Suppress("unused")
val unlockProPatch = bytecodePatch(
    name = "Unlock pro",
) {
    compatibleWith("tv.trakt.trakt"("1.1.1"))

    execute {
        remoteUserFingerprint.matchOrThrow.originalClassDef.let { remoteUserClass ->
            arrayOf(isVIPFingerprint, isVIPEPFingerprint).onEach { fingerprint ->
                // Resolve both fingerprints on the same class.
                fingerprint.match(remoteUserClass) ?: throw fingerprint.exception
            }.forEach { fingerprint ->
                // Return true for both VIP check methods.
                fingerprint.match?.method?.addInstructions(
                    0,
                    """
                        const/4 v0, 0x1
                        invoke-static {v0}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
                        move-result-object v1
                        return-object v1
                    """,
                ) ?: throw fingerprint.exception
            }
        }
    }
}
