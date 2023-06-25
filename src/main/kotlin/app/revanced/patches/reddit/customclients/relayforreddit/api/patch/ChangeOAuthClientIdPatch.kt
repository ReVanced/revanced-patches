package app.revanced.patches.reddit.customclients.relayforreddit.api.patch

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprintResult
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patches.reddit.customclients.AbstractChangeOAuthClientIdPatch
import app.revanced.patches.reddit.customclients.ChangeOAuthClientIdPatchAnnotation
import app.revanced.patches.reddit.customclients.relayforreddit.api.fingerprints.GetClientIdFingerprint

@ChangeOAuthClientIdPatchAnnotation
@Compatibility([Package("free.reddit.news"), Package("reddit.news")])
class ChangeOAuthClientIdPatch : AbstractChangeOAuthClientIdPatch(
    "dbrady://relay", Options, listOf(GetClientIdFingerprint)
) {
    override fun List<MethodFingerprintResult>.patch(context: BytecodeContext): PatchResult {
        first().mutableMethod.replaceInstruction(0, "const-string v0, \"$clientId\"")

        return PatchResultSuccess()
    }

    companion object Options : AbstractChangeOAuthClientIdPatch.Options.ChangeOAuthClientIdOptionsContainer()
}