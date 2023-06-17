package app.revanced.patches.reddit.customclients.boostforreddit.api.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.*
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.reddit.customclients.AbstractChangeOAuthClientIdPatch
import app.revanced.patches.reddit.customclients.boostforreddit.api.fingerprints.GetClientIdFingerprint

@Patch
@Name("change-oauth-client-id")
@Description("Changes the OAuth client ID in Boost for Reddit.")
@Compatibility([Package("com.rubenmayayo.reddit")])
@Version("0.0.1")
class ChangeOAuthClientIdPatch : AbstractChangeOAuthClientIdPatch(
    "http://rubenmayayo.com",
    GetClientIdFingerprint
) {
    override fun MethodFingerprint.patch(context: BytecodeContext): PatchResult {
        result?.mutableMethod?.addInstructions(
            0,
            """
                     const-string v0, "$clientId"
                     return-object v0
                """
        ) ?: return toErrorResult()

        return PatchResultSuccess()
    }

    companion object : Options.ChangeOAuthClientIdOptionsContainer()
}
