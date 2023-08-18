package app.revanced.patches.reddit.customclients.baconreader.api.patch

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Package
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprintResult
import app.revanced.patches.reddit.customclients.AbstractSpoofClientPatch
import app.revanced.patches.reddit.customclients.SpoofClientAnnotation
import app.revanced.patches.reddit.customclients.baconreader.api.fingerprints.GetAuthorizationUrlFingerprint
import app.revanced.patches.reddit.customclients.baconreader.api.fingerprints.RequestTokenFingerprint
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction


@SpoofClientAnnotation
@Description("Spoofs the client in order to allow logging in. " +
        "The OAuth application type has to be \"Installed app\" " +
        "and the redirect URI has to be set to \"http://baconreader.com/auth\".")
@Compatibility(
    [
        Package("com.onelouder.baconreader"),
        Package("com.onelouder.baconreader.premium")
    ]
)
class SpoofClientPatch : AbstractSpoofClientPatch(
    "http://baconreader.com/auth", Options, listOf(GetAuthorizationUrlFingerprint, RequestTokenFingerprint)
) {

    override fun List<MethodFingerprintResult>.patchClientId(context: BytecodeContext) {
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
    }

    companion object Options : AbstractSpoofClientPatch.Options.SpoofClientOptionsContainer()
}
