package app.revanced.patches.reddit.customclients.redditisfun.api

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint.MethodFingerprintResult
import app.revanced.patcher.fingerprint.MethodFingerprintResult.MethodFingerprintScanResult.StringsScanResult.StringMatch
import app.revanced.patches.reddit.customclients.BaseSpoofClientPatch
import app.revanced.patches.reddit.customclients.redditisfun.api.fingerprints.BasicAuthorizationFingerprint
import app.revanced.patches.reddit.customclients.redditisfun.api.fingerprints.BuildAuthorizationStringFingerprint
import app.revanced.patches.reddit.customclients.redditisfun.api.fingerprints.GetUserAgentFingerprint
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference
@Suppress("unused")
object SpoofClientPatch : BaseSpoofClientPatch(
    redirectUri = "redditisfun://auth",
    clientIdFingerprints = setOf(BuildAuthorizationStringFingerprint, BasicAuthorizationFingerprint),
    userAgentFingerprints = setOf(GetUserAgentFingerprint),
    compatiblePackages = setOf(
        CompatiblePackage("com.andrewshu.android.reddit"),
        CompatiblePackage("com.andrewshu.android.redditdonation"),
    ),
) {
    override fun Set<MethodFingerprintResult>.patchClientId(context: BytecodeContext) {
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

    override fun Set<MethodFingerprintResult>.patchUserAgent(context: BytecodeContext) {
        // Use a random user agent.
        val randomName = (0..100000).random()
        val userAgent = "$randomName:app.revanced.$randomName:v1.0.0 (by /u/revanced)"

        first().mutableMethod.addInstructions(
            0,
            """
                const-string v0, "$userAgent"
                return-object v0
            """,
        )
    }

    override fun Set<MethodFingerprintResult>.patchMiscellaneous(context: BytecodeContext) {
        // Reddit messed up and does not append a redirect uri to the authorization url to old.reddit.com/login.
        // Replace old.reddit.com with ssl.reddit.com to fix this.
        BuildAuthorizationStringFingerprint.result!!.mutableMethod.apply {
            val index = indexOfFirstInstruction {
                getReference<StringReference>()?.contains("old.reddit.com") == true
            }

            val targetRegister = getInstruction<OneRegisterInstruction>(index).registerA
            replaceInstruction(
                index,
                "const-string v$targetRegister, \"https://ssl.reddit.com/api/v1/authorize.compact\"",
            )
        }
    }
}
