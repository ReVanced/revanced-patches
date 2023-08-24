package app.revanced.patches.trakt.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.trakt.annotations.UnlockProCompatibility
import app.revanced.patches.trakt.fingerprints.IsVIPEPFingerprint
import app.revanced.patches.trakt.fingerprints.IsVIPFingerprint
import app.revanced.patches.trakt.fingerprints.RemoteUserFingerprint

@Patch
@Name("Unlock pro")
@Description("Unlocks pro features.")
@UnlockProCompatibility
class UnlockProPatch : BytecodePatch(
    listOf(RemoteUserFingerprint)
) {
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

    private companion object {
        const val RETURN_TRUE_INSTRUCTIONS =
            """
                const/4 v0, 0x1
                invoke-static {v0}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
                move-result-object v1
                return-object v1
            """
    }
}