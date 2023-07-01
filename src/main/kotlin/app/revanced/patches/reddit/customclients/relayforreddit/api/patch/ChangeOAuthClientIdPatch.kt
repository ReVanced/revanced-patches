package app.revanced.patches.reddit.customclients.relayforreddit.api.patch

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
import app.revanced.patches.reddit.customclients.relayforreddit.api.fingerprints.GetLoggedInBearerTokenFingerprint
import app.revanced.patches.reddit.customclients.relayforreddit.api.fingerprints.GetLoggedOutBearerTokenFingerprint
import app.revanced.patches.reddit.customclients.relayforreddit.api.fingerprints.GetRefreshTokenFingerprint
import app.revanced.patches.reddit.customclients.relayforreddit.api.fingerprints.LoginActivityClientIdFingerprint
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction

@ChangeOAuthClientIdPatchAnnotation
@Description("Changes the OAuth client ID. " +
        "The OAuth application type has to be \"Installed app\" " +
        "and the redirect URI has to be set to \"dbrady://relay\".")
@Compatibility([Package("free.reddit.news"), Package("reddit.news")])
class ChangeOAuthClientIdPatch : AbstractChangeOAuthClientIdPatch(
    "dbrady://relay",
    Options,
    listOf(
        LoginActivityClientIdFingerprint,
        GetLoggedInBearerTokenFingerprint,
        GetLoggedOutBearerTokenFingerprint,
        GetRefreshTokenFingerprint
    )
) {
    override fun List<MethodFingerprintResult>.patch(context: BytecodeContext): PatchResult {
        forEach {
            val clientIdIndex = it.scanResult.stringsScanResult!!.matches.first().index
            it.mutableMethod.apply {
                val clientIdRegister = getInstruction<OneRegisterInstruction>(clientIdIndex).registerA

                it.mutableMethod.replaceInstruction(
                    clientIdIndex,
                    "const-string v$clientIdRegister, \"$clientId\""
                )
            }
        }

        return PatchResultSuccess()
    }

    companion object Options : AbstractChangeOAuthClientIdPatch.Options.ChangeOAuthClientIdOptionsContainer()
}