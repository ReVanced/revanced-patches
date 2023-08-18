package app.revanced.patches.reddit.customclients.infinityforreddit.api.patch

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Package
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprintResult
import app.revanced.patches.reddit.customclients.AbstractSpoofClientPatch
import app.revanced.patches.reddit.customclients.SpoofClientAnnotation
import app.revanced.patches.reddit.customclients.infinityforreddit.api.fingerprints.GetHttpBasicAuthHeaderFingerprint
import app.revanced.patches.reddit.customclients.infinityforreddit.api.fingerprints.LoginActivityOnCreateFingerprint
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@SpoofClientAnnotation
@Description("Spoofs the client in order to allow logging in. " +
        "The OAuth application type has to be \"Installed app\" " +
        "and the redirect URI has to be set to \"infinity://localhost\".")
@Compatibility([Package("ml.docilealligator.infinityforreddit")])
class SpoofClientPatch : AbstractSpoofClientPatch(
    "infinity://localhost",
    Options,
    listOf(GetHttpBasicAuthHeaderFingerprint, LoginActivityOnCreateFingerprint)
) {
    override fun List<MethodFingerprintResult>.patchClientId(context: BytecodeContext) {
        forEach {
            val clientIdIndex = it.scanResult.stringsScanResult!!.matches.first().index
            it.mutableMethod.apply {
                val oAuthClientIdRegister = getInstruction<OneRegisterInstruction>(clientIdIndex).registerA

                replaceInstruction(
                    clientIdIndex,
                    "const-string v$oAuthClientIdRegister, \"$clientId\""
                )
            }
        }
    }

    companion object Options : AbstractSpoofClientPatch.Options.SpoofClientOptionsContainer()
}