package app.revanced.patches.reddit.customclients.joeyforreddit.api

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstructions
import app.revanced.patcher.fingerprint.MethodFingerprintResult
import app.revanced.patches.reddit.customclients.BaseSpoofClientPatch
import app.revanced.patches.reddit.customclients.joeyforreddit.api.fingerprints.AuthUtilityUserAgent
import app.revanced.patches.reddit.customclients.joeyforreddit.api.fingerprints.GetClientIdFingerprint
import app.revanced.patches.reddit.customclients.joeyforreddit.detection.piracy.DisablePiracyDetectionPatch


@Suppress("unused")
object SpoofClientPatch : BaseSpoofClientPatch(
    redirectUri = "https://127.0.0.1:65023/authorize_callback",
    clientIdFingerprints = setOf(GetClientIdFingerprint),
    userAgentFingerprints = setOf(AuthUtilityUserAgent),
    compatiblePackages = setOf(
        CompatiblePackage("o.o.joey"),
        CompatiblePackage("o.o.joey.pro"),
        CompatiblePackage("o.o.joey.dev")
    ),
    dependencies = setOf(DisablePiracyDetectionPatch::class)
) {
    override fun Set<MethodFingerprintResult>.patchClientId(context: BytecodeContext) {
        first().mutableMethod.addInstructions(
            0,
            """
                const-string v0, "$clientId"
                return-object v0
            """
        )
    }

    override fun Set<MethodFingerprintResult>.patchUserAgent(context: BytecodeContext) {
        // Use a random user agent.
        val randomName = (0..100000).random()
        val userAgent = "$randomName:app.revanced.$randomName:v1.0.0 (by /u/revanced)"

        first().mutableMethod.replaceInstructions(
            0,
            """
                const-string v0, "$userAgent"
                return-object v0
            """,
        )
    }
}