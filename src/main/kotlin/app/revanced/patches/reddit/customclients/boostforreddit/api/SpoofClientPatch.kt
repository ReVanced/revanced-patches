package app.revanced.patches.reddit.customclients.boostforreddit.api

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.fingerprint.MethodFingerprintResult
import app.revanced.patches.reddit.customclients.BaseSpoofClientPatch
import app.revanced.patches.reddit.customclients.boostforreddit.api.fingerprints.GetClientIdFingerprint
import app.revanced.patches.reddit.customclients.boostforreddit.api.fingerprints.JRAWUserAgent

@Suppress("unused")
object SpoofClientPatch : BaseSpoofClientPatch(
    redirectUri = "http://rubenmayayo.com",
    clientIdFingerprints = setOf(GetClientIdFingerprint),
    userAgentFingerprints = setOf(JRAWUserAgent),
    compatiblePackages = setOf(CompatiblePackage("com.rubenmayayo.reddit")),
) {
    override fun Set<MethodFingerprintResult>.patchClientId(context: BytecodeContext) {
        first().mutableMethod.addInstructions(
            0,
            """
                 const-string v0, "$clientId"
                 return-object v0
            """,
        )
    }

    override fun Set<MethodFingerprintResult>.patchUserAgent(context: BytecodeContext) {
        // Use a random user agent.
        val randomName = (0..100000).random()

        first().mutableMethod.addInstructions(
            1,
            "const-string v3, \"$randomName\"",
        )
    }
}
