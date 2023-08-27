package app.revanced.patches.reddit.customclients.relayforreddit.api.patch

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Package
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprintResult
import app.revanced.patches.reddit.customclients.AbstractSpoofClientPatch
import app.revanced.patches.reddit.customclients.SpoofClientAnnotation
import app.revanced.patches.reddit.customclients.relayforreddit.api.fingerprints.GetLoggedInBearerTokenFingerprint
import app.revanced.patches.reddit.customclients.relayforreddit.api.fingerprints.GetLoggedOutBearerTokenFingerprint
import app.revanced.patches.reddit.customclients.relayforreddit.api.fingerprints.GetRefreshTokenFingerprint
import app.revanced.patches.reddit.customclients.relayforreddit.api.fingerprints.LoginActivityClientIdFingerprint
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@SpoofClientAnnotation
@Description("Spoofs the client in order to allow logging in. " +
        "The OAuth application type has to be \"Installed app\" " +
        "and the redirect URI has to be set to \"dbrady://relay\".")
@Compatibility([Package("free.reddit.news"), Package("reddit.news")])
class SpoofClientPatch : AbstractSpoofClientPatch(
    "dbrady://relay",
    Options,
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

    companion object Options : AbstractSpoofClientPatch.Options.SpoofClientOptionsContainer()
}