package app.revanced.patches.reddit.customclients.boostforreddit.api

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.fingerprint.MethodFingerprintResult
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.reddit.customclients.AbstractSpoofClientPatch
import app.revanced.patches.reddit.customclients.Constants.OAUTH_USER_AGENT
import app.revanced.patches.reddit.customclients.boostforreddit.api.fingerprints.GetClientIdFingerprint
import app.revanced.patches.reddit.customclients.boostforreddit.api.fingerprints.LoginActivityOnCreateFingerprint

@Patch(
    name = "Spoof client",
    description = "Restores functionality of the app by using custom client ID's.",
    compatiblePackages = [CompatiblePackage("com.rubenmayayo.reddit")]
)
@Suppress("unused")
object SpoofClientPatch : AbstractSpoofClientPatch(
    "http://rubenmayayo.com",
    clientIdFingerprints = listOf(GetClientIdFingerprint),
    userAgentFingerprints = listOf(LoginActivityOnCreateFingerprint)
) {
    override fun List<MethodFingerprintResult>.patchClientId(context: BytecodeContext) {
        first().mutableMethod.addInstructions(
            0,
            """
                 const-string v0, "$clientId"
                 return-object v0
            """
        )
    }

    override fun List<MethodFingerprintResult>.patchUserAgent(context: BytecodeContext) {
        first().let { result ->
            result.mutableMethod.apply {
                val insertIndex = result.scanResult.patternScanResult!!.endIndex

                addInstructions(
                    insertIndex,
                    """
                        const-string v7, "$OAUTH_USER_AGENT"
                        invoke-virtual {v4, v7}, Landroid/webkit/WebSettings;->setUserAgentString(Ljava/lang/String;)V
                    """
                )
            }
        }
    }
}
