package app.revanced.patches.reddit.customclients.redditisfun.api.patch

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Package
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprintResult
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprintResult.MethodFingerprintScanResult.StringsScanResult.StringMatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patches.reddit.customclients.AbstractChangeOAuthClientIdPatch
import app.revanced.patches.reddit.customclients.ChangeOAuthClientIdPatchAnnotation
import app.revanced.patches.reddit.customclients.redditisfun.api.fingerprints.BasicAuthorizationFingerprint
import app.revanced.patches.reddit.customclients.redditisfun.api.fingerprints.BuildAuthorizationStringFingerprint
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction

@ChangeOAuthClientIdPatchAnnotation
@Description("Changes the OAuth client ID. " +
        "The OAuth application type has to be \"Installed app\" " +
        "and the redirect URI has to be set to \"redditisfun://auth\".")
@Compatibility([Package("com.andrewshu.android.reddit"), Package("com.andrewshu.android.redditdonation")])
class ChangeOAuthClientIdPatch : AbstractChangeOAuthClientIdPatch(
    "redditisfun://auth",
    Options,
    listOf(
        BuildAuthorizationStringFingerprint,
        BasicAuthorizationFingerprint,
    )
) {
    override fun List<MethodFingerprintResult>.patch(context: BytecodeContext): PatchResult {
        /**
         * Replaces a one register instruction with a const-string instruction
         * at the index returned by [getReplacementIndex].
         *
         * @param string The string to replace the instruction with.
         * @param getReplacementIndex A function that returns the index of the instruction to replace
         * using the [StringMatch] list from the [MethodFingerprintResult].
         */
        fun MethodFingerprintResult.replaceWith(
            string: String,
            getReplacementIndex: List<StringMatch>.() -> Int,
        ) = mutableMethod.apply {
            val replacementIndex = scanResult.stringsScanResult!!.matches.getReplacementIndex()
            val clientIdRegister = getInstruction<OneRegisterInstruction>(replacementIndex).registerA

            replaceInstruction(replacementIndex, "const-string v$clientIdRegister, \"$string\"")
        }

        // Patch OAuth authorization.
        first().replaceWith(clientId!!) { first().index + 4 }

        // Path basic authorization.
        last().replaceWith("$clientId:") { last().index + 7 }

        return PatchResultSuccess()
    }

    companion object Options : AbstractChangeOAuthClientIdPatch.Options.ChangeOAuthClientIdOptionsContainer()
}