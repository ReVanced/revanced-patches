package app.revanced.patches.reddit.customclients.joeyforreddit.api.patch

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Package
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprintResult
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.reddit.customclients.AbstractChangeOAuthClientIdPatch
import app.revanced.patches.reddit.customclients.ChangeOAuthClientIdPatchAnnotation
import app.revanced.patches.reddit.customclients.joeyforreddit.api.fingerprints.GetClientIdFingerprint
import app.revanced.patches.reddit.customclients.joeyforreddit.detection.piracy.patch.DisablePiracyDetectionPatch

@ChangeOAuthClientIdPatchAnnotation
@Description(
    "Changes the OAuth client ID. " +
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
class ChangeOAuthClientIdPatch : AbstractChangeOAuthClientIdPatch(
    "https://127.0.0.1:65023/authorize_callback", Options, listOf(GetClientIdFingerprint)
) {
    override fun List<MethodFingerprintResult>.patch(context: BytecodeContext): PatchResult {
        first().mutableMethod.addInstructions(
            0,
            """
                const-string v0, "$clientId"
                return-object v0
            """
        )

        return PatchResultSuccess()
    }

    companion object Options : AbstractChangeOAuthClientIdPatch.Options.ChangeOAuthClientIdOptionsContainer()
}