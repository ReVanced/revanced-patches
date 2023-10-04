package app.revanced.patches.trakt

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.trakt.fingerprints.IsVIPEPFingerprint
import app.revanced.patches.trakt.fingerprints.IsVIPFingerprint
import app.revanced.patches.trakt.fingerprints.RemoteUserFingerprint

@Patch(
    name = "Unlock pro",
    compatiblePackages = [CompatiblePackage("tv.trakt.trakt", ["1.1.1"])]
)
@Suppress("unused")
object UnlockProPatch : BytecodePatch(
    setOf(RemoteUserFingerprint)
) {
    private const val RETURN_TRUE_INSTRUCTIONS =
        """
            const/4 v0, 0x1
            invoke-static {v0}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
            move-result-object v1
            return-object v1
        """

    override fun execute(context: BytecodeContext) {
        RemoteUserFingerprint.result?.classDef?.let { remoteUserClass ->
            arrayOf(IsVIPFingerprint, IsVIPEPFingerprint).onEach { fingerprint ->
                // Resolve both fingerprints on the same class.
                if (!fingerprint.resolve(context, remoteUserClass))
                    throw fingerprint.exception
            }.forEach { fingerprint ->
                // Return true for both VIP check methods.
                fingerprint.result?.mutableMethod?.addInstructions(0, RETURN_TRUE_INSTRUCTIONS)
                    ?: throw fingerprint.exception
            }
        } ?: throw RemoteUserFingerprint.exception
    }
}