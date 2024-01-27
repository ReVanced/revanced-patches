package app.revanced.patches.reddit.customclients.baconreader.api

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint.MethodFingerprintResult
import app.revanced.patches.reddit.customclients.BaseSpoofClientPatch
import app.revanced.patches.reddit.customclients.baconreader.api.fingerprints.GetAuthorizationUrlFingerprint
import app.revanced.patches.reddit.customclients.baconreader.api.fingerprints.RequestTokenFingerprint
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction


@Suppress("unused")
object SpoofClientPatch : BaseSpoofClientPatch(
    redirectUri = "http://baconreader.com/auth",
    clientIdFingerprints = setOf(GetAuthorizationUrlFingerprint, RequestTokenFingerprint),
    compatiblePackages = setOf(
        CompatiblePackage("com.onelouder.baconreader"),
        CompatiblePackage("com.onelouder.baconreader.premium")
    )
) {
    override fun Set<MethodFingerprintResult>.patchClientId(context: BytecodeContext) {
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
