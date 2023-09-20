package app.revanced.patches.reddit.customclients.joeyforreddit.api

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprintResult
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.reddit.customclients.AbstractSpoofClientPatch
import app.revanced.patches.reddit.customclients.joeyforreddit.api.fingerprints.GetClientIdFingerprint
import app.revanced.patches.reddit.customclients.joeyforreddit.detection.piracy.DisablePiracyDetectionPatch


@Patch(
    name = "Spoof client",
    description = "Spoofs the client in order to allow logging in. " +
            "The OAuth application type has to be \"Installed app\" " +
            "and the redirect URI has to be set to \"https://127.0.0.1:65023/authorize_callback\".",
    dependencies = [DisablePiracyDetectionPatch::class],
    compatiblePackages = [
        CompatiblePackage("o.o.joey"),
        CompatiblePackage("o.o.joey.pro"),
        CompatiblePackage("o.o.joey.dev")
    ]
)
@Suppress("unused")
object SpoofClientPatch : AbstractSpoofClientPatch(
    "https://127.0.0.1:65023/authorize_callback", listOf(GetClientIdFingerprint)
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