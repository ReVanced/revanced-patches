package app.revanced.patches.reddit.customclients.redditisfun.api.patch

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Package
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprintResult
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprintResult.MethodFingerprintScanResult.StringsScanResult.StringMatch
import app.revanced.patches.reddit.customclients.AbstractSpoofClientPatch
import app.revanced.patches.reddit.customclients.SpoofClientAnnotation
import app.revanced.patches.reddit.customclients.redditisfun.api.fingerprints.BasicAuthorizationFingerprint
import app.revanced.patches.reddit.customclients.redditisfun.api.fingerprints.BuildAuthorizationStringFingerprint
import app.revanced.patches.reddit.customclients.redditisfun.api.fingerprints.GetUserAgentFingerprint
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@SpoofClientAnnotation
@Description("Spoofs the client in order to allow logging in. " +
        "The OAuth application type has to be \"Installed app\" " +
        "and the redirect URI has to be set to \"redditisfun://auth\".")
@Compatibility([Package("com.andrewshu.android.reddit"), Package("com.andrewshu.android.redditdonation")])
class SpoofClientPatch : AbstractSpoofClientPatch(
    "redditisfun://auth",
    Options,
    listOf(BuildAuthorizationStringFingerprint, BasicAuthorizationFingerprint),
    listOf(GetUserAgentFingerprint)
) {
    override fun List<MethodFingerprintResult>.patchClientId(context: BytecodeContext) {
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
    }

    override fun List<MethodFingerprintResult>.patchUserAgent(context: BytecodeContext) {
        // Use a random user agent.
        val randomName = (0..100000).random()
        val userAgent = "android:app.revanced.$randomName:v1.0.0 (by /u/revanced)"

        first().mutableMethod.addInstructions(
            0,
            """
                const-string v0, "$userAgent"
                return-object v0
            """
        )
    }

    companion object Options : AbstractSpoofClientPatch.Options.SpoofClientOptionsContainer()
}