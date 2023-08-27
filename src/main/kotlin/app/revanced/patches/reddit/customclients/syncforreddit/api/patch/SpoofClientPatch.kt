package app.revanced.patches.reddit.customclients.syncforreddit.api.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Package
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprintResult
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.reddit.customclients.AbstractSpoofClientPatch
import app.revanced.patches.reddit.customclients.SpoofClientAnnotation
import app.revanced.patches.reddit.customclients.syncforreddit.api.fingerprints.GetAuthorizationStringFingerprint
import app.revanced.patches.reddit.customclients.syncforreddit.api.fingerprints.GetBearerTokenFingerprint
import app.revanced.patches.reddit.customclients.syncforreddit.detection.piracy.patch.DisablePiracyDetectionPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import java.util.*

@SpoofClientAnnotation
@Description("Spoofs the client in order to allow logging in. " +
        "The OAuth application type has to be \"Installed app\" " +
        "and the redirect URI has to be set to \"http://redditsync/auth\".")
@Compatibility(
    [
        Package("com.laurencedawson.reddit_sync"),
        Package("com.laurencedawson.reddit_sync.pro"),
        Package("com.laurencedawson.reddit_sync.dev")
    ]
)
@DependsOn([DisablePiracyDetectionPatch::class])
class SpoofClientPatch : AbstractSpoofClientPatch(
    "http://redditsync/auth", Options, listOf(GetAuthorizationStringFingerprint)
) {
    override fun List<MethodFingerprintResult>.patchClientId(context: BytecodeContext) {
        forEach { fingerprintResult ->
            fingerprintResult.also { result ->
                GetBearerTokenFingerprint.also { it.resolve(context, result.classDef) }.result?.mutableMethod?.apply {
                    val auth = Base64.getEncoder().encodeToString("$clientId:".toByteArray(Charsets.UTF_8))
                    addInstructions(
                        0,
                        """
                         const-string v0, "Basic $auth"
                         return-object v0
                    """
                    )
                } ?: throw GetBearerTokenFingerprint.exception
            }.let {
                val occurrenceIndex = it.scanResult.stringsScanResult!!.matches.first().index

                it.mutableMethod.apply {
                    val authorizationStringInstruction = getInstruction<ReferenceInstruction>(occurrenceIndex)
                    val targetRegister = (authorizationStringInstruction as OneRegisterInstruction).registerA
                    val reference = authorizationStringInstruction.reference as StringReference

                    val newAuthorizationUrl = reference.string.replace(
                        "client_id=.*?&".toRegex(),
                        "client_id=$clientId&"
                    )

                    replaceInstruction(
                        occurrenceIndex,
                        "const-string v$targetRegister, \"$newAuthorizationUrl\""
                    )
                }
            }
        }
    }

    companion object Options : AbstractSpoofClientPatch.Options.SpoofClientOptionsContainer()
}
