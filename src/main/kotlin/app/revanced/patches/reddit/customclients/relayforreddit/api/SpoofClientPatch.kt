package app.revanced.patches.reddit.customclients.relayforreddit.api

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprintResult
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.reddit.customclients.AbstractSpoofClientPatch
import app.revanced.patches.reddit.customclients.relayforreddit.api.fingerprints.GetLoggedInBearerTokenFingerprint
import app.revanced.patches.reddit.customclients.relayforreddit.api.fingerprints.GetLoggedOutBearerTokenFingerprint
import app.revanced.patches.reddit.customclients.relayforreddit.api.fingerprints.GetRefreshTokenFingerprint
import app.revanced.patches.reddit.customclients.relayforreddit.api.fingerprints.LoginActivityClientIdFingerprint
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Spoof client",
    description = "Spoofs the client in order to allow logging in. " +
            "The OAuth application type has to be \"Installed app\" " +
            "and the redirect URI has to be set to \"dbrady://relay\".",
    compatiblePackages = [
        CompatiblePackage("free.reddit.news"),
        CompatiblePackage("reddit.news")
    ]
)
@Suppress("unused")
object SpoofClientPatch : AbstractSpoofClientPatch(
    "dbrady://relay",
    listOf(
        LoginActivityClientIdFingerprint,
        GetLoggedInBearerTokenFingerprint,
        GetLoggedOutBearerTokenFingerprint,
        GetRefreshTokenFingerprint
    )
) {
    override fun List<MethodFingerprintResult>.patchClientId(context: BytecodeContext) {
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
    }
}