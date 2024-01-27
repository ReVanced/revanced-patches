package app.revanced.patches.reddit.customclients.slide.api

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.fingerprint.MethodFingerprintResult
import app.revanced.patches.reddit.customclients.BaseSpoofClientPatch
import app.revanced.patches.reddit.customclients.boostforreddit.api.fingerprints.GetClientIdFingerprint


@Suppress("unused")
object SpoofClientPatch : BaseSpoofClientPatch(
    redirectUri = "http://www.ccrama.me",
    clientIdFingerprints = setOf(GetClientIdFingerprint),
    compatiblePackages = setOf(CompatiblePackage("me.ccrama.redditslide"))
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
}
