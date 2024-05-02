package app.revanced.patches.trakt

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.trakt.fingerprints.isVIPEPFingerprint
import app.revanced.patches.trakt.fingerprints.isVIPFingerprint
import app.revanced.patches.trakt.fingerprints.remoteUserFingerprint
import app.revanced.util.exception

private const val RETURN_TRUE_INSTRUCTIONS =
    """
            const/4 v0, 0x1
            invoke-static {v0}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
            move-result-object v1
            return-object v1
        """

@Suppress("unused")
val unlockProPatch = bytecodePatch(
    name = "Unlock pro",
) {
    compatibleWith("tv.trakt.trakt"("1.1.1"))

    val remoteUserResult by remoteUserFingerprint

    execute { context ->
        remoteUserResult.classDef.let { remoteUserClass ->
            arrayOf(isVIPFingerprint, isVIPEPFingerprint).onEach { fingerprint ->
                // Resolve both fingerprints on the same class.
                if (!fingerprint.resolve(context, remoteUserClass)) {
                    throw fingerprint.exception
                }
            }.forEach { fingerprint ->
                // Return true for both VIP check methods.
                fingerprint.result?.mutableMethod?.addInstructions(0, RETURN_TRUE_INSTRUCTIONS)
                    ?: throw fingerprint.exception
            }
        }
    }
}
