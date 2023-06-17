package app.revanced.patches.reddit.customclients.syncforreddit.api.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.*
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.reddit.customclients.AbstractChangeOAuthClientIdPatch
import app.revanced.patches.reddit.customclients.syncforreddit.api.fingerprints.GetAuthorizationStringFingerprint
import app.revanced.patches.reddit.customclients.syncforreddit.api.fingerprints.GetBearerTokenFingerprint
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.reference.StringReference
import java.util.*


@Patch
@Name("change-oauth-client-id")
@Description("Changes the OAuth client ID.")
@Compatibility([Package("com.laurencedawson.reddit_sync"), Package("com.laurencedawson.reddit_sync.pro")])
@Version("0.0.1")
class ChangeOAuthClientIdPatch : AbstractChangeOAuthClientIdPatch(
    "http://redditsync/auth",
    GetAuthorizationStringFingerprint,
) {
    override fun MethodFingerprint.patch(context: BytecodeContext): PatchResult {
        result?.also { result ->
            GetBearerTokenFingerprint.also { it.resolve(context, result.classDef) }.result?.mutableMethod?.apply {
                val auth = Base64.getEncoder().encodeToString("$clientId:".toByteArray(Charsets.UTF_8))
                addInstructions(
                    0,
                    """
                         const-string v0, "Basic $auth"
                         return-object v0
                    """
                )
            } ?: return GetBearerTokenFingerprint.toErrorResult()
        }?.let {
            val occurrenceIndex = it.scanResult.stringsScanResult!!.matches.first().index

            it.mutableMethod.apply {
                val authorizationStringInstruction = getInstruction<ReferenceInstruction>(occurrenceIndex)
                val targetRegister = (authorizationStringInstruction as OneRegisterInstruction).registerA
                val reference = authorizationStringInstruction.reference as StringReference

                val newAuthorizationUrl = reference.string.replace(
                    "client_id=.*?&".toRegex(),
                    "client_id=${clientId!!}&"
                )

                replaceInstruction(
                    occurrenceIndex,
                    "const-string v$targetRegister, \"$newAuthorizationUrl\""
                )
            }
        } ?: return toErrorResult()

        return PatchResultSuccess()
    }

    companion object : Options.ChangeOAuthClientIdOptionsContainer()
}
