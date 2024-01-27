package app.revanced.patches.reddit.customclients.syncforreddit.api

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint.MethodFingerprintResult
import app.revanced.patches.reddit.customclients.BaseSpoofClientPatch
import app.revanced.patches.reddit.customclients.syncforreddit.api.fingerprints.GetAuthorizationStringFingerprint
import app.revanced.patches.reddit.customclients.syncforreddit.api.fingerprints.GetBearerTokenFingerprint
import app.revanced.patches.reddit.customclients.syncforreddit.api.fingerprints.ImgurImageAPIFingerprint
import app.revanced.patches.reddit.customclients.syncforreddit.api.fingerprints.LoadBrowserURLFingerprint
import app.revanced.patches.reddit.customclients.syncforreddit.detection.piracy.DisablePiracyDetectionPatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import java.util.*


@Suppress("unused")
object SpoofClientPatch : BaseSpoofClientPatch(
    redirectUri = "http://redditsync/auth",
    miscellaneousFingerprints = setOf(ImgurImageAPIFingerprint),
    clientIdFingerprints = setOf(GetAuthorizationStringFingerprint),
    userAgentFingerprints = setOf(LoadBrowserURLFingerprint),
    compatiblePackages = setOf(
        CompatiblePackage("com.laurencedawson.reddit_sync"),
        CompatiblePackage("com.laurencedawson.reddit_sync.pro"),
        CompatiblePackage("com.laurencedawson.reddit_sync.dev")
    ),
    dependencies = setOf(DisablePiracyDetectionPatch::class)
) {
    override fun Set<MethodFingerprintResult>.patchClientId(context: BytecodeContext) {
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

    // Use the non-commercial Imgur API endpoint.
    override fun Set<MethodFingerprintResult>.patchMiscellaneous(context: BytecodeContext) = first().let {
        val apiUrlIndex = it.scanResult.stringsScanResult!!.matches.first().index

        it.mutableMethod.replaceInstruction(
            apiUrlIndex,
            "const-string v1, \"https://api.imgur.com/3/image\""
        )
    }
}
