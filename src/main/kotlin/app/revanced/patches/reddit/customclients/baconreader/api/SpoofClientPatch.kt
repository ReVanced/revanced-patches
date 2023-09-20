package app.revanced.patches.reddit.customclients.baconreader.api

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprintResult
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.reddit.customclients.AbstractSpoofClientPatch
import app.revanced.patches.reddit.customclients.baconreader.api.fingerprints.GetAuthorizationUrlFingerprint
import app.revanced.patches.reddit.customclients.baconreader.api.fingerprints.RequestTokenFingerprint
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction


@Patch(
    name = "Spoof client",
    description = "Spoofs the client in order to allow logging in. " +
            "The OAuth application type has to be \"Installed app\" " +
            "and the redirect URI has to be set to \"http://baconreader.com/auth\".",
    compatiblePackages = [
        CompatiblePackage("com.onelouder.baconreader"),
        CompatiblePackage("com.onelouder.baconreader.premium")
    ]
)
@Suppress("unused")
object SpoofClientPatch : AbstractSpoofClientPatch(
    "http://baconreader.com/auth", listOf(GetAuthorizationUrlFingerprint, RequestTokenFingerprint)
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
}
