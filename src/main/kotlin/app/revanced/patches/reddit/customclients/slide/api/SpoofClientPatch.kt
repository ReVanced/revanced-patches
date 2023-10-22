package app.revanced.patches.reddit.customclients.slide.api

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.fingerprint.MethodFingerprintResult
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.reddit.customclients.AbstractSpoofClientPatch
import app.revanced.patches.reddit.customclients.boostforreddit.api.fingerprints.GetClientIdFingerprint

@Patch(
    name = "Spoof client",
    description = "Restores functionality of the app by using custom client ID's.",
    compatiblePackages = [CompatiblePackage("me.ccrama.redditslide")]
)
@Suppress("unused")
object SpoofClientPatch : AbstractSpoofClientPatch(
    "http://www.ccrama.me", listOf(GetClientIdFingerprint)
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
}
