package app.revanced.patches.reddit.customclients.joeyforreddit.api.patch

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Package
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprintResult
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.reddit.customclients.AbstractSpoofClientPatch
import app.revanced.patches.reddit.customclients.SpoofClientAnnotation
import app.revanced.patches.reddit.customclients.joeyforreddit.api.fingerprints.GetClientIdFingerprint
import app.revanced.patches.reddit.customclients.joeyforreddit.detection.piracy.patch.DisablePiracyDetectionPatch

@SpoofClientAnnotation
@Description(
    "Spoofs the client in order to allow logging in. " +
            "The OAuth application type has to be \"Installed app\" " +
            "and the redirect URI has to be set to \"https://127.0.0.1:65023/authorize_callback\"."
)
@Compatibility(
    [
        Package("o.o.joey"),
        Package("o.o.joey.pro"),
        Package("o.o.joey.dev")
    ]
)
@DependsOn([DisablePiracyDetectionPatch::class])
class SpoofClientPatch : AbstractSpoofClientPatch(
    "https://127.0.0.1:65023/authorize_callback", Options, listOf(GetClientIdFingerprint)
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

    companion object Options : AbstractSpoofClientPatch.Options.SpoofClientOptionsContainer()
}