package app.revanced.patches.reddit.customclients.baconreader.api.patch

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Package
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprintResult
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patches.reddit.customclients.AbstractChangeOAuthClientIdPatch
import app.revanced.patches.reddit.customclients.ChangeOAuthClientIdPatchAnnotation
import app.revanced.patches.reddit.customclients.baconreader.api.fingerprints.GetAuthorizationUrlFingerprint
import app.revanced.patches.reddit.customclients.baconreader.api.fingerprints.RequestTokenFingerprint
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction


@ChangeOAuthClientIdPatchAnnotation
@Description("Changes the OAuth client ID. " +
        "The OAuth application type has to be \"Installed app\" " +
        "and the redirect URI has to be set to \"http://baconreader.com/auth\".")
@Compatibility(
    [
        Package("com.onelouder.baconreader"),
        Package("com.onelouder.baconreader.premium")
    ]
)
class ChangeOAuthClientIdPatch : AbstractChangeOAuthClientIdPatch(
    "http://baconreader.com/auth", Options, listOf(GetAuthorizationUrlFingerprint, RequestTokenFingerprint)
) {

    override fun List<MethodFingerprintResult>.patch(context: BytecodeContext): PatchResult {
        fun MethodFingerprintResult.patch(replacementString: String) {
            val clientIdIndex = scanResult.stringsScanResult!!.matches.first().index

            mutableMethod.apply {
                val clientIdRegister = getInstruction<OneRegisterInstruction>(clientIdIndex).registerA
                replaceInstruction(
                    clientIdIndex,
                    "const-string v$clientIdRegister, \"$replacementString\""
                )
            }
        }

        // Patch client id in authorization url.
        first().patch("client_id=$clientId")

        // Patch client id for access token request.
        last().patch(clientId!!)

        return PatchResultSuccess()
    }

    companion object Options : AbstractChangeOAuthClientIdPatch.Options.ChangeOAuthClientIdOptionsContainer()
}
