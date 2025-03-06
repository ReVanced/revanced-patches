package app.revanced.patches.reddit.customclients.boostforreddit.api

import app.revanced.patcher.Fingerprint
import app.revanced.patcher.Match
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patches.reddit.customclients.spoofClientPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

val spoofClientPatch = spoofClientPatch() { clientIdOption, redirectUriOption, userAgentOption ->
    compatibleWith("com.rubenmayayo.reddit")
    execute {
        // region Patch client id.

        getClientIdFingerprint.method.addInstructions(
            0,
            """
                 const-string v0, "$clientIdOption"
                 return-object v0
            """,
        )

        // endregion

        // region Patch user agent and redirect URI.

        val stringReplacements = mapOf(
            // Take advantage of the fact that String.format() will ignore extraneous parameters.
            "%s:%s:%s (by /u/%s)" to userAgentOption,
            "http://rubenmayayo.com" to redirectUriOption
        )

        val replaceStrings = fun(methodFingerprint: Fingerprint) : Unit {
            methodFingerprint.method.apply {
                methodFingerprint.stringMatches!!.forEach { match ->
                    val replacement = stringReplacements[match.string]
                    val register = getInstruction<OneRegisterInstruction>(match.index).registerA

                    replaceInstruction(match.index, "const-string v$register, \"$replacement\"")
                }
            }
        }

        replaceStrings(buildUserAgentFingerprint)
        replaceStrings(loginActivityOnCreateFingerprint)
        replaceStrings(loginActivityAShouldOverrideUrlLoadingFingerprint)

        // endregion
    }
}
