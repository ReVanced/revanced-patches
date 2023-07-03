package app.revanced.patches.trakt.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.trakt.annotations.UnlockProCompatibility
import app.revanced.patches.trakt.fingerprints.IsVIPEPFingerprint
import app.revanced.patches.trakt.fingerprints.IsVIPFingerprint
import app.revanced.patches.trakt.fingerprints.RemoteUserFingerprint

@Patch
@Name("unlock-pro")
@Description("Unlocks pro features.")
@UnlockProCompatibility
@Version("0.0.1")
class UnlockProPatch : BytecodePatch(
    listOf(RemoteUserFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        RemoteUserFingerprint.result?.classDef?.let { remoteUserClass ->
            arrayOf(IsVIPFingerprint, IsVIPEPFingerprint).onEach { fingerprint ->
                // Resolve both fingerprints on the same class.
                if (!fingerprint.resolve(context, remoteUserClass))
                    throw fingerprint.toErrorResult()
            }.forEach { fingerprint ->
                // Return true for both VIP check methods.
                fingerprint.result?.mutableMethod?.addInstructions(0, RETURN_TRUE_INSTRUCTIONS)
                    ?: return fingerprint.toErrorResult()
            }
        } ?: return RemoteUserFingerprint.toErrorResult()

        return PatchResultSuccess()
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