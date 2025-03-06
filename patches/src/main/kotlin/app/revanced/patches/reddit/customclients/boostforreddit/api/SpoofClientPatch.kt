package app.revanced.patches.reddit.customclients.boostforreddit.api

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patches.reddit.customclients.spoofClientPatch
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

import app.revanced.patcher.patch.BytecodePatchBuilder
import app.revanced.patcher.patch.Option
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.stringOption

@Suppress("unused")
val spoofClientPatch = bytecodePatch(
    name = "Spoof client",
) {
    compatibleWith("com.rubenmayayo.reddit")

    val redirectUri = "http://rubenmayayo.com"
    val clientId by stringOption(
        "client-id",
        null,
        null,
        "OAuth client ID",
        "The Reddit OAuth client ID. " +
                "You can get your client ID from https://www.reddit.com/prefs/apps. " +
                "The application type has to be \"Installed app\" " +
                "and the redirect URI has to be set to \"$redirectUri\".",
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

        // region Patch user agent.

        // Take advantage of the fact that String.format() will ignore extraneous parameters.
        val stringReplacements = mapOf(
            "%s:%s:%s (by /u/%s)" to userAgent,
        )

        buildUserAgentFingerprint.method.apply {
            buildUserAgentFingerprint.stringMatches!!.forEach { match ->
                val replacement = stringReplacements[match.string]
                val register = getInstruction<OneRegisterInstruction>(match.index).registerA

                replaceInstruction(match.index, "const-string v$register, \"$replacement\"")
            }
        }

        // endregion
    }
}
