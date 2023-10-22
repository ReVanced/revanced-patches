package app.revanced.patches.reddit.customclients.syncforreddit.api

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint.MethodFingerprintResult
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.reddit.customclients.AbstractSpoofClientPatch
import app.revanced.patches.reddit.customclients.Constants.OAUTH_USER_AGENT
import app.revanced.patches.reddit.customclients.syncforreddit.api.fingerprints.GetAuthorizationStringFingerprint
import app.revanced.patches.reddit.customclients.syncforreddit.api.fingerprints.GetBearerTokenFingerprint
import app.revanced.patches.reddit.customclients.syncforreddit.api.fingerprints.ImgurImageAPIFingerprint
import app.revanced.patches.reddit.customclients.syncforreddit.api.fingerprints.LoadBrowserURLFingerprint
import app.revanced.patches.reddit.customclients.syncforreddit.detection.piracy.DisablePiracyDetectionPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import java.util.*

@Patch(
    name = "Spoof client",
    description = "Restores functionality of the app by using custom client ID's.",
    dependencies = [DisablePiracyDetectionPatch::class],
    compatiblePackages =  [
        CompatiblePackage("com.laurencedawson.reddit_sync"),
        CompatiblePackage("com.laurencedawson.reddit_sync.pro"),
        CompatiblePackage("com.laurencedawson.reddit_sync.dev")
    ]
)
@Suppress("unused")
object SpoofClientPatch : AbstractSpoofClientPatch(
    "http://redditsync/auth",
    clientIdFingerprints = listOf(GetAuthorizationStringFingerprint),
    userAgentFingerprints = listOf(LoadBrowserURLFingerprint),
    miscellaneousFingerprints = listOf(ImgurImageAPIFingerprint)
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

    // Use the non-commercial Imgur API endpoint.
    override fun List<MethodFingerprintResult>.patchMiscellaneous(context: BytecodeContext) = first().let {
        val apiUrlIndex = it.scanResult.stringsScanResult!!.matches.first().index

        it.mutableMethod.replaceInstruction(
            apiUrlIndex,
            "const-string v1, \"https://api.imgur.com/3/image\""
        )
    }

    override fun List<MethodFingerprintResult>.patchUserAgent(context: BytecodeContext) {
        first().let { result ->
            val insertIndex = result.scanResult.patternScanResult!!.startIndex

            result.mutableMethod.addInstructions(
                insertIndex,
                """
                    const-string v0, "$OAUTH_USER_AGENT"
                    invoke-virtual {p1, v0}, Landroid/webkit/WebSettings;->setUserAgentString(Ljava/lang/String;)V
                """
            )
        }
    }
}
