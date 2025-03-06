package app.revanced.patches.reddit.customclients.boostforreddit.api

import app.revanced.patcher.Fingerprint
import app.revanced.patcher.Match
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatchBuilder
import app.revanced.patcher.patch.Option
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.stringOption
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val spoofClientPatch = bytecodePatch(
    name = "Spoof client",
) {
    compatibleWith("com.rubenmayayo.reddit")

    val redirectUri by stringOption(
        "redirect-uri",
        "http://127.0.0.1:8080",
        null,
        "Redirect URI",
        "The Reddit OAuth redirect URI. " +
                "If you don't know what this means, don't change it. " +
                "Make sure you update the \"Installed app\" you created at " +
                "https://www.reddit.com/prefs/apps to have the same redirect URI " +
                "as what you put here.",
        true,
    )

    val clientId by stringOption(
        "client-id",
        null,
        null,
        "OAuth client ID",
        "The Reddit OAuth client ID. " +
                "You can get a client ID by creating an \"Installed app\" at " +
                "https://www.reddit.com/prefs/apps. The redirect URI should be set to " +
                "\"http://127.0.0.1:8080\" or whatever is specified for the redirect URI " +
                "option.",
        true,
    )

    val userAgent by stringOption(
        "user-agent",
        null,
        null,
        "User agent",
        "The app's user agent. User agent should be in the format " +
                "\"<platform>:<app id>:<version> (by /u/<username)\".",
        true,
    )

    execute {
        // region Patch client id.

        getClientIdFingerprint.method.addInstructions(
            0,
            """
                 const-string v0, "$clientId"
                 return-object v0
            """,
        )

        // endregion

        // region Patch user agent and redirect URI.

        val stringReplacements = mapOf(
            // Take advantage of the fact that String.format() will ignore extraneous parameters.
            "%s:%s:%s (by /u/%s)" to userAgent,
            "http://rubenmayayo.com" to redirectUri
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
